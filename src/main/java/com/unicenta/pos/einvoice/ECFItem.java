package com.unicenta.pos.einvoice;

public class ECFItem {
    private int numeroLinea;
    private String nombreItem;
    private double cantidadItem;
    private double precioItem;
    private double montoItem;
    private double itbis; // Add getter/setter

    // Getters and Setters
    public double getITBIS() {
    return itbis;
}

public void setITBIS(double itbis) {
    this.itbis = itbis;
}
    public int getNumeroLinea() { return numeroLinea; }
    public void setNumeroLinea(int numeroLinea) { this.numeroLinea = numeroLinea; }

    public String getNombreItem() { return nombreItem; }
    public void setNombreItem(String nombreItem) { this.nombreItem = nombreItem; }

    public double getCantidadItem() { return cantidadItem; }
    public void setCantidadItem(double cantidadItem) { this.cantidadItem = cantidadItem; }

    public double getPrecioItem() { return precioItem; }
    public void setPrecioItem(double precioItem) { this.precioItem = precioItem; }

    public double getMontoItem() { return montoItem; }
    public void setMontoItem(double montoItem) { this.montoItem = montoItem; }
}
