package com.unicenta.pos.einvoice;

import java.util.Date;
import java.util.List;

public class ECFData {

    private String tipoECF;
    private String eNCF;
    private Date fechaEmision;

    private String tipoPago;
    private String tipoIngresos;

    private String rncEmisor;
    private String nombreComercial;

    private String rncReceptor;
    private String razonSocialReceptor;

    private List<ECFItem> items;
    private double montoTotal;

    public String getTipoECF() { return tipoECF; }
    public void setTipoECF(String tipoECF) { this.tipoECF = tipoECF; }

    public String geteNCF() { return eNCF; }
    public void seteNCF(String eNCF) { this.eNCF = eNCF; }

    public Date getFechaEmision() {
    return fechaEmision;
}

public void setFechaEmision(Date fechaEmision) {
    this.fechaEmision = fechaEmision;
}


    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public String getTipoIngresos() { return tipoIngresos; }
    public void setTipoIngresos(String tipoIngresos) { this.tipoIngresos = tipoIngresos; }

    public String getRncEmisor() { return rncEmisor; }
    public void setRncEmisor(String rncEmisor) { this.rncEmisor = rncEmisor; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getRncReceptor() { return rncReceptor; }
    public void setRncReceptor(String rncReceptor) { this.rncReceptor = rncReceptor; }

    public String getRazonSocialReceptor() { return razonSocialReceptor; }
    public void setRazonSocialReceptor(String razonSocialReceptor) { this.razonSocialReceptor = razonSocialReceptor; }

    public List<ECFItem> getItems() { return items; }
    public void setItems(List<ECFItem> items) { this.items = items; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    private double totalITBIS;

public double getTotalITBIS() {
    return totalITBIS;
}

public void setTotalITBIS(double totalITBIS) {
    this.totalITBIS = totalITBIS;
}

}
