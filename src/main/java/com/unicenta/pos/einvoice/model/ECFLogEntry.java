package com.unicenta.pos.einvoice.model;

import java.util.Date;

public class ECFLogEntry {
    public String ticketId;
    public String encf;
    public String status;
    public String xmlSignedPath;
    public String xmlResponsePath;
    public String errorMessage;
    public Date timestampSent;
    public Date timestampResponse;
    public int retries;
}
