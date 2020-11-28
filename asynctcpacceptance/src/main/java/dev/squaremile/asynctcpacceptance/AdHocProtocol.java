package dev.squaremile.asynctcpacceptance;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class AdHocProtocol
{
    public static final int NO_OPTIONS = 0;
    public static final int PLEASE_RESPOND_FLAG = 1;

    public static final int OFFSET_OPTIONS = 0;
    public static final int SEND_TIME_OFFSET = 4;
    public static final int CORRELATION_ID_OFFSET = 12;
    public static final int EXTRA_DATA_OFFSET = 20;

    public static final Delineation DELINEATION = new Delineation(Delineation.Type.INT_LITTLE_ENDIAN_FIELD, 0, 0, "");

}
