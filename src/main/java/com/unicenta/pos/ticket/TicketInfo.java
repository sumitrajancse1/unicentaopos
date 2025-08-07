//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.
package com.unicenta.pos.ticket;

import com.unicenta.basic.BasicException;
import com.unicenta.pos.sales.DataLogicReceipts;
import com.unicenta.pos.forms.DataLogicSales;
import com.unicenta.pos.sales.NcfType;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.LocalRes;
import com.unicenta.data.loader.SerializableRead;
import com.unicenta.format.Formats;
import com.unicenta.pos.customers.CustomerInfoExt;
import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.payment.PaymentInfo;
import com.unicenta.pos.payment.PaymentInfoMagcard;
import com.unicenta.pos.payment.PaymentInfoTicket;
import com.unicenta.pos.sales.DataLogicReceipts;
import com.unicenta.pos.sales.NcfType;
import com.unicenta.pos.util.StringUtils;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author adrianromero
 */
public final class TicketInfo implements SerializableRead, Externalizable {

    private static final long serialVersionUID = 2765650092387265178L;

    public static final int RECEIPT_NORMAL = 0;
    public static final int RECEIPT_REFUND = 1;
    public static final int RECEIPT_PAYMENT = 2;
    public static final int RECEIPT_NOSALE = 3;
    
// JG Jun 2017 - contain partial/full refunds    
    public static final int REFUND_NOT = 0; // is a non-refunded ticket    
    public static final int REFUND_PARTIAL = 1;
    public static final int REFUND_ALL = 2;
  
    private static final DateFormat m_dateformat = new SimpleDateFormat("hh:mm");

    private String m_sHost;
    private String m_sId;
    private int tickettype;
    private int m_iTicketId;
    private int m_iPickupId;
    private java.util.Date m_dDate;
    private Properties attributes;
    private UserInfo m_User;
    private Double multiply;
    private CustomerInfoExt m_Customer;
    private String m_sActiveCash;
    private List<TicketLineInfo> m_aLines;
    private List<PaymentInfo> payments;
    private List<TicketTaxInfo> taxes;
    private final String m_sResponse;
    private String loyaltyCardNumber;
    private Boolean oldTicket;
    private boolean tip;
    private PaymentInfoTicket m_paymentInfo;
    private boolean m_isProcessed;
    private final String m_locked;
    private Double nsum;
    private int ticketstatus;
    //Sumit Changes
    private String day;
    private String pickupTime;
    private String ncfType;
    private String orderType;
    private String orderNo;
    private String encf;
private String encfStatus;
private String encfSignature;
private String encfQrPath;
private Date encfSignedAt;
private Date encfSentAt;
private String encfResponsePath;

    // End changes 
    private static String Hostname;

    public static void setHostname(String name) {
        Hostname =name;
    }

    public static String getHostname() {
        return Hostname;
    }

    /** Creates new TicketModel */
    public TicketInfo() {
        m_sId = UUID.randomUUID().toString();
        tickettype = RECEIPT_NORMAL;
        m_iTicketId = 0; // incrementamos
        m_dDate = new Date();
        attributes = new Properties();
        m_User = null;
        m_Customer = null;
        m_sActiveCash = null;
        m_aLines = new ArrayList<>();
        payments = new ArrayList<>();
        taxes = null;
        m_sResponse = null;
        oldTicket=false;

        AppConfig config = new AppConfig(new File(new File(
            System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        config.load();
        tip = Boolean.valueOf(config.getProperty("machine.showTip"));
        m_isProcessed = false;
        m_locked = null;
        ticketstatus = 0;
        // Start Change
        day = "";
        pickupTime = "";
        ncfType = "";
        orderType = "";
        orderNo = "";
        // End change
        encf = "";
encfSignature = "";
encfStatus = "";
encfResponsePath = "";
encfQrPath = "";
encfSignedAt = null;
encfSentAt = null;

        
                
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(m_sId);
        out.writeInt(tickettype);
        out.writeInt(m_iTicketId);
        out.writeObject(m_Customer);
        out.writeObject(m_dDate);
        out.writeObject(attributes);
        out.writeObject(m_aLines);

        out.writeInt(ticketstatus);
        out.writeObject(orderType);
        out.writeObject(day);
        out.writeObject(pickupTime);
        out.writeObject(ncfType);
        out.writeObject(orderNo);
        out.writeObject(encf);
out.writeObject(encfSignature);
out.writeObject(encfSignedAt);
out.writeObject(encfSentAt);
out.writeObject(encfStatus);
out.writeObject(encfResponsePath);
out.writeObject(encfQrPath);

        
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        m_sId = (String) in.readObject();
        tickettype = in.readInt();
        m_iTicketId = in.readInt();
        m_Customer = (CustomerInfoExt) in.readObject();
        m_dDate = (Date) in.readObject();
        attributes = (Properties) in.readObject();
        m_aLines = (List<TicketLineInfo>) in.readObject();
        m_User = null;
        m_sActiveCash = null;
        payments = new ArrayList<>(); // JG June 2102 diamond inference
        taxes = null;

        ticketstatus = in.readInt();
        orderType = (String) in.readObject();
        day = (String) in.readObject();
        pickupTime = (String) in.readObject();
        ncfType = (String) in.readObject();
        orderNo =(String) in.readObject();
        encf = (String) in.readObject();
encfSignature = (String) in.readObject();
encfSignedAt = (Date) in.readObject();
encfSentAt = (Date) in.readObject();
encfStatus = (String) in.readObject();
encfResponsePath = (String) in.readObject();
encfQrPath = (String) in.readObject();

        
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sId = dr.getString(1);
        tickettype = dr.getInt(2);
        m_iTicketId = dr.getInt(3);
        m_dDate = dr.getTimestamp(4);
        m_sActiveCash = dr.getString(5);
        try {
            byte[] img = dr.getBytes(6);
            if (img != null) {
                attributes.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        m_User = new UserInfo(dr.getString(7), dr.getString(8));
        m_Customer = new CustomerInfoExt(dr.getString(9));
        m_aLines = new ArrayList<>();
        payments = new ArrayList<>();
        taxes = null;
        
        ticketstatus = dr.getInt(10);
        orderType = dr.getString(11);
        day = dr.getString(12);
        pickupTime = dr.getString(13);
        ncfType = dr.getString(14);
        orderNo = dr.getString(15);
        encf = dr.getString(16);
encfSignature = dr.getString(17);
encfSignedAt = dr.getTimestamp(18);
encfSentAt = dr.getTimestamp(19);
encfStatus = dr.getString(20);
encfResponsePath = dr.getString(21);
encfQrPath = dr.getString(22);

    }

    /**
     *
     * @return
     */
    public TicketInfo copyTicket() {
        TicketInfo t = new TicketInfo();

        t.tickettype = tickettype;
        t.m_iTicketId = m_iTicketId;
        t.m_dDate = m_dDate;
        t.m_sActiveCash = m_sActiveCash;
        t.attributes = (Properties) attributes.clone();
        t.m_User = m_User;
        t.m_Customer = m_Customer;

        t.m_aLines = new ArrayList<>(); // JG June 2102 diamond inference
        m_aLines.forEach((l) -> {
            t.m_aLines.add(l.copyTicketLine());
        });
        t.refreshLines();

        t.payments = new LinkedList<>(); // JG June 2102 diamond inference
        payments.forEach((p) -> {
            t.payments.add(p.copyPayment());
        });
        t.oldTicket=oldTicket;
        // taxes are not copied, must be calculated again.

        t.ticketstatus = ticketstatus;
        
        t.day = day;
        t.pickupTime =pickupTime;
        t.ncfType = ncfType;
        t.orderType = orderType;
        t.orderNo = orderNo;
        t.encf = this.encf;
t.encfSignature = this.encfSignature;
t.encfSignedAt = this.encfSignedAt;
t.encfSentAt = this.encfSentAt;
t.encfStatus = this.encfStatus;
t.encfResponsePath = this.encfResponsePath;
t.encfQrPath = this.encfQrPath;

        
        return t;
    }

    public String getId() {
        return m_sId;
    }
    //Start Changes
    public String getDay(){
        return day;
    }
    public void setDay(String day){
        this.day = day;
    }
    public String getOrderType(){
        return orderType;
    }
    public void setOrderType(String orderType ){
        this.orderType = orderType;
    }
    public String getOrderNo(){
        if(orderNo.isEmpty()){
            return "";
        } 
        else {
            return orderNo;
        }
    }
    public void setOrderNo(String orderNo ){
        this.orderNo = orderNo;
    }
    public String getENCF() { return encf; }
public void setENCF(String encf) { this.encf = encf; }

public void setENCFStatus(String status) { this.encfStatus = status; }
public String getENCFStatus() { return encfStatus; }

public void setENCFSignature(String sig) { this.encfSignature = sig; }
public String getENCFSignature() { return encfSignature; }

public void setENCFQrPath(String path) { this.encfQrPath = path; }
public String getENCFQrPath() { return encfQrPath; }

public void setENCFSignedAt(Date d) { this.encfSignedAt = d; }
public Date getENCFSignedAt() { return encfSignedAt; }

public void setENCFSentAt(Date d) { this.encfSentAt = d; }
public Date getENCFSentAt() { return encfSentAt; }

public void setENCFResponsePath(String path) { this.encfResponsePath = path; }
public String getENCFResponsePath() { return encfResponsePath; }

    public String getPickupTime(){
        return pickupTime;
    }
    public void setPickupTime(String pickupTime){
        this.pickupTime = pickupTime;
    }
    public String getncfType(){
        //here
        //DataLogicReceipts dataLogicReceipts;
        //DataLogicSales m_dlSales;
        //m_dlSales  = new DataLogicSales();//(DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
       // dataLogicReceipts = (DataLogicReceipts) application.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        //dataLogicReceipts = new DataLogicReceipts();
        String localNcfType;
       //NcfType record ;//= new NcfType();
        //Put Logic to check for the generated NCF when editing an invoice
        //try {
      /*  if (ncfType != null){
            
            if (ncfType.startsWith("B01")){
                
               //localNcfType =  m_dlSales.getRangeType("B01");
               localNcfType="CREDITO FISCAL";
               return localNcfType;
               //return record.m_sNcfType;
            }
            if (ncfType.startsWith("B02")){
                
              // localNcfType =  m_dlSales.getRangeType("B02");
               //return record.ncfType;
                localNcfType = "CONSUMIDOR FINAL";
               return localNcfType;               
//return localNcfType;
            }
            
        }*/
        
        
        return ncfType;
    }
    public void setNcfType(String ncfType){
        this.ncfType= ncfType;
        if (this.ncfType == null || this.ncfType.isEmpty() || !this.ncfType.equals(ncfType)) {
        this.ncfType = ncfType;
    }
    }
    // End Changes

    public int getTicketType() {
        return tickettype;
    }
    public void setTicketType(int tickettype) {
        this.tickettype = tickettype;
    }

    public int getTicketId() {
        return m_iTicketId;
    }
    public void setTicketId(int iTicketId) {
        m_iTicketId = iTicketId;
    }

    public int getTicketStatus() {
        return ticketstatus;
    }
    public void setTicketStatus(int ticketstatus) {
        if (m_iTicketId >0) {
            this.ticketstatus = m_iTicketId;
        }else{
            this.ticketstatus = ticketstatus;
        }
    }
    
    public void setPickupId(int iTicketId) {
        m_iPickupId = iTicketId;
    }

    public int getPickupId() {
        return m_iPickupId;
    }
    
    public String getName(Object info) {
// JG Aug 2014 - Add User info
        List<String> name = new ArrayList<>();        

        String nameprop = getProperty("name"); 
        if (nameprop != null) {
            name.add(nameprop);            
        }
        
        if (m_User != null) {
            name.add(m_User.getName());                        
        }

        if (info == null) {
            if (m_iTicketId == 0) {
                name.add("(" + m_dateformat.format(m_dDate) + " " 
                        + Long.toString(m_dDate.getTime() % 1000) + ")");                
            } else {
                name.add(Integer.toString(m_iTicketId));                
            }
        } else {
            name.add(info.toString());            
        }

        if (m_Customer != null) {        
            name.add(m_Customer.getName());            
        }

        return org.apache.commons.lang.StringUtils.join(name, " - ");        
    }
    
    public String getName() {
        return getName(null);
    }

    public java.util.Date getDate() {
        return m_dDate;
    }

    public void setDate(java.util.Date dDate) {
        m_dDate = dDate;
    }
    
    public String getHost() {
      AppConfig m_config_host =  new AppConfig(new File((System.getProperty("user.home")),
              AppLocal.APP_ID + ".properties"));        
      m_config_host.load();
      String machineHostname =(m_config_host.getProperty("machine.hostname"));
      m_config_host = null;
      return machineHostname;
    }
    
    public UserInfo getUser() {
        return m_User;
    }

    public void setUser(UserInfo value) {
        m_User = value;
    }

    public CustomerInfoExt getCustomer() {
        return m_Customer;
    }

    public void setCustomer(CustomerInfoExt value) {
        m_Customer = value;
    }

    public String getCustomerId() {
        if (m_Customer == null) {
            return null;
        } else {
            return m_Customer.getId();
        }
    }
    
    public String getTransactionID(){
        return (getPayments().size()>0)
            ? ( getPayments().get(getPayments().size()-1) ).getTransactionID()
            : StringUtils.getCardNumber(); //random transaction ID
    }
    
    public String getReturnMessage(){
        return ( (getPayments().get(getPayments().size()-1)) instanceof PaymentInfoMagcard )
            ? ((PaymentInfoMagcard)(getPayments().get(getPayments().size()-1))).getReturnMessage()
            : LocalRes.getIntString("button.OK");
    }

    public void setActiveCash(String value) {
        m_sActiveCash = value;
    }

    public String getActiveCash() {
        return m_sActiveCash;
    }

    public String getProperty(String key) {
        return attributes.getProperty(key);
    }

    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }

    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }

    public Properties getProperties() {
        return attributes;
    }

    public TicketLineInfo getLine(int index) {
        return m_aLines.get(index);
    }

    public void addLine(TicketLineInfo oLine) {
        oLine.setTicket(m_sId, m_aLines.size());
        m_aLines.add(oLine);
    }

    public void insertLine(int index, TicketLineInfo oLine) {
        m_aLines.add(index, oLine);
        refreshLines();
    }

    public void setLine(int index, TicketLineInfo oLine) {
        oLine.setTicket(m_sId, index);
        m_aLines.set(index, oLine);
    }

    public void removeLine(int index) {
        m_aLines.remove(index);
        refreshLines();
       
    }

    public void refreshLines() {
        for (int i = 0; i < m_aLines.size(); i++) {
            getLine(i).setTicket(m_sId, i);
        }
    }

    public int getLinesCount() {
        return m_aLines.size();
    }
    
    public double getArticlesCount() {
        double dArticles = 0.0;
        TicketLineInfo oLine;

        for (Iterator<TicketLineInfo> i = m_aLines.iterator(); i.hasNext();) {
            oLine = i.next();
            dArticles += oLine.getMultiply();
        }

        return dArticles;
    }

    public double getSubTotal() {
        double sum = 0.0;
        sum = m_aLines.stream().map((line) -> 
                line.getSubValue()).reduce(sum, (accumulator, _item) -> 
                        accumulator + _item);
        return sum;
    }

    public double getTax() {

        double sum = 0.0;
        if (hasTaxesCalculated()) {
            for (TicketTaxInfo tax : taxes) {
                sum += tax.getTax(); // Taxes are already rounded...
                nsum = sum;
            }
        } else {
            sum = m_aLines.stream().map((line) -> 
                    line.getTax()).reduce(sum, (accumulator, _item) -> 
                            accumulator + _item);
            }
        return sum;
    }

    public double getTotal() {
        return getSubTotal() + getTax();

    }
    
    public double getServiceCharge() {
        return (getTotal() + getTax());        

    }
    
    public double getTotalPaid() {
        double sum = 0.0;
        sum = payments.stream().filter((p) -> 
                (!"debtpaid".equals(p.getName()))).map((p) -> 
                        p.getTotal()).reduce(sum, (accumulator, _item) -> 
                                accumulator + _item);
        return sum;
          }

    public double getTendered() {
        return getTotalPaid();
    }    

    public List<TicketLineInfo> getLines() {
        return m_aLines;
    }

    public void setLines(List<TicketLineInfo> l) {
        m_aLines = l;
    }

    public List<PaymentInfo> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInfo> l) {
        payments = l;
    }

    public void resetPayments() {
        payments = new ArrayList<>(); // JG June 2102 diamond inference
    }

    public List<TicketTaxInfo> getTaxes() {
        return taxes;
    }

    public boolean hasTaxesCalculated() {
        return taxes != null;
    }

    public void setTaxes(List<TicketTaxInfo> l) {
        taxes = l;
    }

    public void resetTaxes() {
        taxes = null;
    }

    public void setTip(boolean tips) {
        tip = tips;
    }

    public boolean hasTip() {
        return tip;
    }

    public void setIsProcessed(boolean isP) {
        m_isProcessed = isP;
    }

    public TicketTaxInfo getTaxLine(TaxInfo tax) {

        for (TicketTaxInfo taxline : taxes) {
            if (tax.getId().equals(taxline.getTaxInfo().getId())) {
                return taxline;
            }
        }

        return new TicketTaxInfo(tax);
    }

    public TicketTaxInfo[] getTaxLines() {

        Map<String, TicketTaxInfo> m = new HashMap<>();

        TicketLineInfo oLine;
        for (Iterator<TicketLineInfo> i = m_aLines.iterator(); i.hasNext();) {
            oLine = i.next();

            TicketTaxInfo t = m.get(oLine.getTaxInfo().getId());
            if (t == null) {
                t = new TicketTaxInfo(oLine.getTaxInfo());
                m.put(t.getTaxInfo().getId(), t);
            }
            t.add(oLine.getSubValue());
        }

        // return dSuma;       
        Collection<TicketTaxInfo> avalues = m.values();
        return avalues.toArray(new TicketTaxInfo[avalues.size()]);
    }

    public String printId() {
      
      AppConfig m_config =  new AppConfig(new File(
              (System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));        
      m_config.load();
      String receiptSize =(m_config.getProperty("till.receiptsize"));
      String receiptPrefix =(m_config.getProperty("till.receiptprefix"));
     
      m_config =null;

        if (m_iTicketId > 0) {
            String tmpTicketId=Integer.toString(m_iTicketId);
            if (receiptSize == null || (Integer.parseInt(receiptSize) <= tmpTicketId.length())){
                if (receiptPrefix != null){
                    tmpTicketId=receiptPrefix+tmpTicketId;
                } 
                return tmpTicketId;
            }            
            while (tmpTicketId.length()<Integer.parseInt(receiptSize)){
                tmpTicketId="0"+tmpTicketId;
            }
            if (receiptPrefix != null){
                    tmpTicketId=receiptPrefix+tmpTicketId;
            }             
            return tmpTicketId;
        } else {
            return "";
        }
    }

    public String printDate() {
        return Formats.TIMESTAMP.formatValue(m_dDate);
    }

    public String printUser() {
        return m_User == null ? "" : m_User.getName();
        
    }

    public String printHost() {
        return m_sHost;
       }
    
    
// Added JDL 28.05.13 for loyalty card functions

    /**
     *
     */
        public void clearCardNumber(){
        loyaltyCardNumber=null;
    }
    
    /**
     *
     * @param cardNumber
     */
    public void setLoyaltyCardNumber(String cardNumber){
        loyaltyCardNumber=cardNumber;
    }
    
    /**
     *
     * @return
     */
    public String getLoyaltyCardNumber(){
        return (loyaltyCardNumber);
    }
         
    public String printCustomer() {
        return m_Customer == null ? "" : m_Customer.getName();
    }

    public String printPhone1() {
        return m_Customer == null ? "" : m_Customer.getPhone1();
    }    

    public String printArticlesCount() {
        return Formats.DOUBLE.formatValue(getArticlesCount());
    }

    public String printSubTotal() {
        return Formats.CURRENCY.formatValue(getSubTotal());
    }

    public String printTax() {
        return Formats.CURRENCY.formatValue(getTax());
    }

    public String printTotal() {
        return Formats.CURRENCY.formatValue(getTotal());
    }
    
    public String printDay(){
       
        //return getDay();
        return day == null ? "" : getDay();
    }
    public String printNcf(){
     //   return getncfType();
        return ncfType == null ? "" : getncfType();
    }
    public String printOrderNo(){
        return orderNo == null ? "" : getOrderNo();
    }
    public String printNcfHeader(){
     //   return getncfType();
     String localNcfType=" ";
     
       if (ncfType !=null) {
          //Code here
        if (ncfType.startsWith("B01")){
            localNcfType ="FACTURA PARA CREDITO FISCAL";
            return localNcfType;
        }
        else if (ncfType.startsWith("B02")){
            localNcfType ="FACTURA PARA CONSUMIDOR FINAL";
            return localNcfType;
        }
        else if (ncfType.startsWith("B14")){
            localNcfType ="FACTURA PARA REG�MENES ESPECIALES";
            return localNcfType;
        }
       }
       return    localNcfType;
        //return ncfType == null ? "" : getncfType();
    }
    public String printOrderType(){
        //return getOrderType();
        return orderType == null ? "" : getOrderType();
        
    }
    public String printPickuptime(){
       // return getPickupTime();
       return pickupTime == null ? "" : getPickupTime();
    }
    
    public String printQRCode() {
    return encfQrPath == null ? "" : encfQrPath; // RAW URL for barcode
}

public String printQRCodeEscaped() {
    return encfQrPath == null ? "" : encfQrPath.replace("&", "&amp;"); // Escaped for XML <text>
}
    public String printTotalPaid() {
        return Formats.CURRENCY.formatValue(getTotalPaid());
    }

    public String printTendered() {
        return Formats.CURRENCY.formatValue(getTendered());
    }

    public String VoucherReturned(){
        return Formats.CURRENCY.formatValue(getTotalPaid()- getTotal());
    }

    public boolean getOldTicket() {
	return (oldTicket);
    }

    public void setOldTicket(Boolean otState) {
	oldTicket = otState;
    }
    
    public String getTicketHeaderFooterData(String data) {
        AppConfig m_config = new AppConfig(new File((System.getProperty("user.home"))
                , AppLocal.APP_ID + ".properties"));        
        m_config.load();
        String row =(m_config.getProperty("tkt."+data));
        
        return row;
    }    
    
    public String printTicketHeaderLine1() {
        String lineData = getTicketHeaderFooterData("header1");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketHeaderLine2() {
        String lineData = getTicketHeaderFooterData("header2");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketHeaderLine3() {
        String lineData = getTicketHeaderFooterData("header3");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketHeaderLine4() {
        String lineData = getTicketHeaderFooterData("header4");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketHeaderLine5() {
        String lineData = getTicketHeaderFooterData("header5");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketHeaderLine6() {
        String lineData = getTicketHeaderFooterData("header6");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketFooterLine1() {
        String lineData = getTicketHeaderFooterData("footer1");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketFooterLine2() {
        String lineData = getTicketHeaderFooterData("footer2");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketFooterLine3() {
        String lineData = getTicketHeaderFooterData("footer3");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketFooterLine4() {
        String lineData = getTicketHeaderFooterData("footer4");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketFooterLine5() {
        String lineData = getTicketHeaderFooterData("footer5");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
    
    public String printTicketFooterLine6() {
        String lineData = getTicketHeaderFooterData("footer6");
        
        if(lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }
/* Start Change Sumit */
   public boolean hasServiceCharge() {
        if (!m_aLines.stream().anyMatch(line -> (line.getProductName().equalsIgnoreCase("Service Charge")))) {
        } else {
            return true;
        } // Assuming getProductName() returns the name or description of the product
         // Assuming getProductName() returns the name or description of the product
    return false;
}
   public String getServiceChargeName() {
    return "Service Charge";
}
   public double calculateServiceCharge() {
    // Implement your logic to calculate the service charge amount
    // For example, you can calculate it based on a fixed percentage of the subtotal
    double subtotal = getSubTotal();
    double serviceChargePercentage = 0.10; // 5% service charge
    return subtotal * serviceChargePercentage;
}
   public String printServiceCharge() {
    if (hasServiceCharge()) {
        System.out.println(getServiceChargeName() + "\t\t\t\t" + Formats.CURRENCY.formatValue(calculateServiceCharge()));
        String str,s;
        double cal=0.0;
        cal=calculateServiceCharge();
        s = String.format("%f", cal);
        str =  getServiceChargeName() + "\t\t" + s;
       // return getServiceChargeName() + "\t\t\t\t" + Formats.CURRENCY.formatValue(calculateServiceCharge();
       return str;
    }
   return " ";
    
    }
   public double getSubTotalWithoutService() {
    double sum = 0.0;
    for (TicketLineInfo line : m_aLines) {
        if (!line.getProductName().equalsIgnoreCase("Service Charge")) {
            sum += line.getSubValue();
        }
    }
    return sum;
}
   public String printSubTotalWithoutService() {
        return Formats.CURRENCY.formatValue(getSubTotalWithoutService());
    }
  
   private TicketLineInfo getServiceChargeLine() {
    for (TicketLineInfo line : this.getLines()) {
        if (line.isServiceChargeLine()) {
            return line;
        }
    }
    return null;
}

}
   /* End Change Sumit */    
   
