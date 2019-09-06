package com.example.detectordemovimentos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class BackgroundService extends Service implements Runnable {

    private ArrayList<Double> eixoX = new ArrayList<Double>();
    private ArrayList<Double> eixoY = new ArrayList<Double>();
    private ArrayList<Double> eixoZ = new ArrayList<Double>();
    private CDDL cddl;
    private String ID = "movimento", lastLocation, selectedSensor;
    private Subscriber subscriberAcelerometro, subscriberLocation;
    private Publisher publisherResult;
    private J48 classificador;
    private boolean executando;
    Connection connectionServer, connection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        executando = true;

        //criando notificação de execução
        Toast.makeText(this, "Serviço iniciado", Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChanel = new NotificationChannel("Detectando movimento", "Serviço de detecção", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(serviceChanel);
        }

        Notification notification = new NotificationCompat.Builder(this, "Detectando movimento")
                .setContentTitle("Serviço de detecção")
                .setContentText("Executando")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);

        //instanciando classificador
        try {
            classificador = (J48) SerializationHelper.read(getAssets().open("tree.model"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //iniciando serviço em nova thread
        new Thread(this).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executando = false;
        cddl.stopAllSensors();
        cddl.stopLocationSensor();
        Toast.makeText(this, "Serviço parado", Toast.LENGTH_LONG).show();
    }

    private void calculo() {
        AcelerometroCalculos a = new AcelerometroCalculos(eixoX, eixoY, eixoZ);
        eixoX.clear();
        eixoY.clear();
        eixoZ.clear();

        //Criar nova instância de classificação
        ArrayList<Attribute> att = new ArrayList<>();
        att.add(new Attribute("eixoX"));
        att.add(new Attribute("eixoY"));
        att.add(new Attribute("eixoZ"));
        att.add(new Attribute("desvioPadraoX"));
        att.add(new Attribute("desvioPadraoY"));
        att.add(new Attribute("desvioPadraoZ"));
        att.add(new Attribute("CorrelacaoXY"));
        att.add(new Attribute("CorrelacaoXZ"));
        att.add(new Attribute("CorrelacaoYZ"));
        ArrayList<String> type = new ArrayList<>();
        type.add("PARADO");
        type.add("ANDANDO");
        type.add("CORRENDO");
        att.add(new Attribute("Type", type));
        Instances classificar = new Instances("Movimento", att, 0);
        classificar.setClassIndex(classificar.numAttributes() - 1);

        double[] valores = {a.getMediaX(), a.getMediaY(), a.getMediaZ(), a.getDesvioX(), a.getDesvioY(), a.getDesvioZ(), a.getCorrelacaoXY(), a.getCorrelacaoXZ(), a.getCorrelacaoYZ()};
        DenseInstance novo = new DenseInstance(1.0, valores);
        classificar.add(novo);
        novo.setDataset(classificar);

        //realizando classificação
        Double movimento = -1.0;
        try {
            movimento = classificador.classifyInstance(novo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //mandando para o broker na nuvem o resultado com a localização
        Message m = new Message();
        m.setServiceName("classificação");
        m.setServiceValue(movimento + "," + lastLocation);
        publisherResult.publish(m);
    }

    private void configCDDL() {
        //Criar conexão microbroker
        connection = ConnectionFactory.createConnection();
        connection.setHost(CDDL.startMicroBroker());
        connection.setClientId(ID);
        connection.connect();

        //Criar conexão broker nuvem
        connectionServer = ConnectionFactory.createConnection();
        connectionServer.setHost("postman.cloudmqtt.com");
        connectionServer.setUsername("oirdlgbr");
        connectionServer.setPassword("iNlLp3WtIaU6");
        connectionServer.setPort("13905");
        connectionServer.setClientId(ID);
        connectionServer.connect();

        //cria um novo publiser
        publisherResult = PublisherFactory.createPublisher();
        publisherResult.addConnection(connectionServer);

        //inicia o cddl
        cddl = CDDL.getInstance();
        cddl.setConnection(connection);
        cddl.setContext(this);
        cddl.startService();
        cddl.startLocationSensor();
        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_ID);
        selectedSensor = cddl.getInternalSensorList().get(0).getName();

        //Cria um subscriber para os dados do acelerômetro
        subscriberAcelerometro = SubscriberFactory.createSubscriber();
        subscriberAcelerometro.addConnection(connection);
        //subscriberAcelerometro.setFilter("SELECT * FROM SensorDataMessage.win:time_batch(5 s) where serviceName = '" + selectedSensor + "' ");
        subscriberAcelerometro.setSubscriberListener(new ISubscriberListener() {
            @Override
            public void onMessageArrived(Message message) {
                Object[] valor = message.getServiceValue();
                eixoX.add(0, Double.parseDouble(valor[0].toString()));
                eixoY.add(0, Double.parseDouble(valor[1].toString()));
                eixoZ.add(0, Double.parseDouble(valor[2].toString()));
            }
        });

        //Cria um subscriber para os dados de localização
        subscriberLocation = SubscriberFactory.createSubscriber();
        subscriberLocation.addConnection(connection);
        subscriberLocation.setFilter("SELECT * FROM SensorDataMessage where (serviceName = 'Location' AND accuracy <= 23)");
        subscriberLocation.setSubscriberListener(new ISubscriberListener() {
            @Override
            public void onMessageArrived(Message message) {
                Object[] valores = message.getServiceValue();
                lastLocation = valores[0].toString() + "," + valores[1].toString();
            }
        });
    }

    private void startSelectedSensor() {
        cddl.startSensor(selectedSensor);
        cddl.startLocationSensor();

        subscriberAcelerometro.subscribeServiceByName(selectedSensor);
        subscriberLocation.subscribeServiceByName("Location");
    }

    @Override
    public void run() {
        configCDDL();
        startSelectedSensor();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                calculo();
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 5000,5000);


    }
}
