/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unicenta.pos.sales;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import java.io.*;
import com.unicenta.data.loader.SerializableRead;
import java.io.IOException;
import java.io.ObjectOutput;
import com.unicenta.pos.sales.DataLogicReceipts;


/**
 *
 * @author Sumit Rajan Singh
 * * This is created to handle the NCF functionality.
 */
public class NcfType implements SerializableRead ,Externalizable {
   
    private String m_sNcfType;
    private String m_sFixText;
    private int m_iNcfSequence;
    
    /** Creates a new instance of NcfType */
    public NcfType() {
        m_sNcfType = null;
        m_sFixText = null;
        
    }
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        m_sNcfType = (String) in.readObject();
        m_sFixText = (String) in.readObject();
        m_iNcfSequence = in.readInt();
        
    }
    
    /**
     *
     * @return
     */
    public Object getNcfType() {
        DataLogicReceipts dataLogicReceipts = new DataLogicReceipts();
       // dataLogicReceipts = (DataLogicReceipts) application.getBean("com.unicenta.pos.sales.DataLogicReceipts");
        //dataLogicReceipts = new DataLogicReceipts();
       NcfType record ;//= new NcfType();
       String localNcfType;
        //Put Logic to check for the generated NCF when editing an invoice
        
        /*if (m_sNcfType != null){
            
            if (m_sNcfType.startsWith("B01")){
                
               //localNcfType =  dataLogicReceipts.getRangeType("B01");
               //return record.m_sNcfType;
               localNcfType = "CREDITO FISCAL";
               return localNcfType;
            }
            if (m_sNcfType.startsWith("B02")){
                
               //localNcfType =  dataLogicReceipts.getRangeType("B02");
               //return record.m_sNcfType;
               localNcfType = "CONSUMIDOR FINAL";
               return localNcfType;
            }
            
        }*/
        
         
             
        return m_sNcfType;
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sNcfType = dr.getString(1);
        m_sFixText = dr.getString(2);
        m_iNcfSequence = dr.getInt(3);
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(m_sNcfType);
        out.writeObject(m_sFixText);
        out.writeInt(m_iNcfSequence);
        
        
    }

    /**
     *
     * @param sNcfType
     */
    public void setNcfType(String sNcfType) {
        m_sNcfType = sNcfType;
    }
    
    /**
     *
     * @return
     */
    public String getFixText() {
        return m_sFixText;
    }

    /**
     *
     * @return
     */
    public int getNcfSequence() {
        return m_iNcfSequence;
    }
    
    /**
     *
     * @param sFixText
     */
    public void setFixText(String sFixText) {
        m_sFixText = sFixText;
    }  

        
    /**
     *
     * @param iNcfSequence
     */
    public void setNcfSequence(int iNcfSequence) {
        m_iNcfSequence = iNcfSequence;
    } 
    
    
    
}
