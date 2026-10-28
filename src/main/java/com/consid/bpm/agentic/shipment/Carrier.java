package com.consid.bpm.agentic.shipment;

public enum Carrier {
    DHL("DHL", "https://www.dhl.com/de-en/home/tracking/tracking-parcel.html?tracking-id=%s"),
    DPD("DPD", "https://tracking.dpd.de/status/en_US/parcel/%s"),
    UPS("UPS", "https://www.ups.com/track?tracknum=%s"),
    POSTNORD("PostNord", "https://www.postnord.se/en/our-tools/track-and-trace?shipmentId=%s"),
    GLS("GLS", "https://gls-group.com/track/%s");

    private final String displayName;
    private final String trackingUrlTemplate;

    Carrier(String displayName, String trackingUrlTemplate) {
        this.displayName = displayName;
        this.trackingUrlTemplate = trackingUrlTemplate;
    }

    public String displayName() {
        return displayName;
    }

    public String trackingUrl(String trackingNumber) {
        return trackingUrlTemplate.formatted(trackingNumber);
    }
}
