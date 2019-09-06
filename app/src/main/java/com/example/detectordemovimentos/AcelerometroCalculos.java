package com.example.detectordemovimentos;

import java.util.ArrayList;

public class AcelerometroCalculos {

    private int length;
    private ArrayList<Double> eixoX = new ArrayList<Double>();
    private ArrayList<Double> eixoY = new ArrayList<Double>();
    private ArrayList<Double> eixoZ = new ArrayList<Double>();
    private Double mediaX, mediaY, mediaZ, desvioX, desvioY, desvioZ, correlacaoXY, correlacaoXZ, correlacaoYZ;

    public AcelerometroCalculos(ArrayList<Double> eixoX, ArrayList<Double> eixoY, ArrayList<Double> eixoZ) {
        this.eixoX = eixoX;
        this.eixoY = eixoY;
        this.eixoZ = eixoZ;

        length = eixoX.size();
        //   convert(dados);
        mediaX = media(eixoX);
        mediaY = media(eixoY);
        mediaZ = media(eixoZ);
        desvioPadrao();
        correlacaoXY = correlacaoEntreOndas(mediaX, mediaY, desvioX, desvioY, eixoX, eixoY);
        correlacaoXZ = correlacaoEntreOndas(mediaX, mediaZ, desvioX, desvioZ, eixoX, eixoZ);
        correlacaoYZ = correlacaoEntreOndas(mediaY, mediaZ, desvioY, desvioZ, eixoY, eixoZ);
    }

//    private void convert(List<String> dados) {
//
//        for (String v: dados) {
//            String[] valor = v.split(",");
//            eixoX.add(Double.parseDouble(valor[0]));
//            eixoY.add(Double.parseDouble(valor[1]));
//            eixoZ.add(Double.parseDouble(valor[2]));
//
//        }
//
//    }

    private Double media(ArrayList<Double> values) {
        Double media = 0.0;

        for (int i = 0; i < values.size(); i++)
            media += values.get(i);

        return media / length;
    }

    private void desvioPadrao() {
        Double acumuladorX = 0.0;
        Double acumuladorY = 0.0;
        Double acumuladorZ = 0.0;


        for (int i = 0; i < length; i++) {
            acumuladorX += Math.pow(eixoX.get(i) - mediaX, 2.0);
            acumuladorY += Math.pow(eixoY.get(i) - mediaY, 2.0);
            acumuladorZ += Math.pow(eixoZ.get(i) - mediaZ, 2.0);
        }

        desvioX = Math.sqrt(acumuladorX / length);
        desvioY = Math.sqrt(acumuladorY / length);
        desvioZ = Math.sqrt(acumuladorZ / length);

    }


    private Double correlacaoEntreOndas(Double media1, Double media2, Double desvio1, Double desvio2, ArrayList<Double> eixo1, ArrayList<Double> eixo2) {
        return (mediaMultiplicacaoValoresXY(eixo1, eixo2) - (media1 * media2)) / (desvio1 * desvio2);
    }


    private Double mediaMultiplicacaoValoresXY(ArrayList<Double> eixo1, ArrayList<Double> eixo2) {
        ArrayList<Double> multValores = new ArrayList<Double>();
        for (int i = 0; i < length; i++) {
            multValores.add(eixo1.get(i) * eixo2.get(i));
        }
        return media(multValores);
    }

    public String getMedidas() {
        return mediaX + "," + mediaY + "," + mediaZ + "," + desvioX + "," + desvioY + "," + desvioZ + "," +
                correlacaoXY + "," + correlacaoXZ + "," + correlacaoYZ;

    }

    public Double getMediaX() {
        return mediaX;
    }

    public void setMediaX(Double mediaX) {
        this.mediaX = mediaX;
    }

    public Double getMediaY() {
        return mediaY;
    }

    public void setMediaY(Double mediaY) {
        this.mediaY = mediaY;
    }

    public Double getMediaZ() {
        return mediaZ;
    }

    public void setMediaZ(Double mediaZ) {
        this.mediaZ = mediaZ;
    }

    public Double getDesvioX() {
        return desvioX;
    }

    public void setDesvioX(Double desvioX) {
        this.desvioX = desvioX;
    }

    public Double getDesvioY() {
        return desvioY;
    }

    public void setDesvioY(Double desvioY) {
        this.desvioY = desvioY;
    }

    public Double getDesvioZ() {
        return desvioZ;
    }

    public void setDesvioZ(Double desvioZ) {
        this.desvioZ = desvioZ;
    }

    public Double getCorrelacaoXY() {
        return correlacaoXY;
    }

    public void setCorrelacaoXY(Double correlacaoXY) {
        this.correlacaoXY = correlacaoXY;
    }

    public Double getCorrelacaoXZ() {
        return correlacaoXZ;
    }

    public void setCorrelacaoXZ(Double correlacaoXZ) {
        this.correlacaoXZ = correlacaoXZ;
    }

    public Double getCorrelacaoYZ() {
        return correlacaoYZ;
    }

    public void setCorrelacaoYZ(Double correlacaoYZ) {
        this.correlacaoYZ = correlacaoYZ;
    }
}
