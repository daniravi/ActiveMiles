package org.imperial.activemilespro.nfc_tag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.Tag;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

import org.imperial.activemilespro.nfc_tag.Ntag_Get_Version.Prod;


/**
 * Class for the different Demos
 *
 * @author NXP67729
 *
 */
public class Ntag_I2C_Demo {
    I2C_Enabled_Commands reader;
    Activity main;
    Tag tag;
    String answer;

    /**
     * Constructor
     *
     * @param tag
     *            Tag with which the Demos should be performed
     * @param main
     *            MainActivity
     */
    public Ntag_I2C_Demo(Tag tag, final Activity main) {
        try {
            if (tag == null) {
                this.main = null;
                this.tag = null;
                return;

            }
            this.main = main;
            this.tag = tag;

            reader = I2C_Enabled_Commands.get(tag);

            if (reader == null) {
                new AlertDialog.Builder(main)
                        .setMessage(
                                "The Tag could not be identified or this NFC device does not support the NFC Forum commands needed to access this tag")
                        .setTitle("Communication failed")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }
                                }).show();
            } else {
                reader.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if the tag is still connected
     *
     * @return Boolean indicating tag connection
     *
     */
    public boolean isConnected() {
        return reader.isConnected();
    }


    /**
     * Checks if the demo is ready to be executed
     *
     * @return Boolean indicating demo readiness
     *
     */
    public boolean isReady() {
        if (tag != null && reader != null)
            return true;
        else
            return false;
    }

    public Prod getProduct() throws IOException {
        return reader.getProduct();
    }


    /**
     * Resets the tag to its delivery values
     *
     * @return Boolean indicating success or error
     *
     */
    public boolean resetTag() {
        boolean success = true;

        try {
            reader.writeDeliveryNdef();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }

        return success;
    }



    /**
     * Reads the whole tag memory content
     *
     * @return Boolean indicating success or error
     */
    public byte[] readTagContent() {
        byte[] bytes = null;

        try {
            // The user memory and the first four pages are displayed
            int memSize = reader.getProduct().getMemsize() + 16;

            // Read all the pages using the fast read method
            bytes = reader.readEEPROM(0, memSize / reader.getBlockSize());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    /**
     * Resets the whole tag memory content (Memory to 00000...)
     *
     * @return Boolean indicating success or error
     */
    public boolean resetTagContent() {
        boolean success = true;

        try {
            byte[] d = new byte[reader.getProduct().getMemsize()];
            reader.writeEEPROM(d);
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        } catch (FormatException e) {
            success = false;
            e.printStackTrace();
        }

        return success;
    }



    private class WriteEmptyNdefTask extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused")
        public Boolean exit = false;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                reader.writeEmptyNdef();
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
                return null;
            }
            return null;
        }

    }


    private NdefMessage creatNdefDefaultMessage()
            throws UnsupportedEncodingException {
        NdefRecord uri_record = NdefRecord
                .createUri("http://www.nxp.com/products/identification_and_security/smart_label_and_tag_ics/ntag/series/NT3H1101_NT3H1201.html");
        String text = "NTAG I2C Demoboard LPC812";
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
        NdefRecord text_record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], payload);

        NdefRecord[] sp_records = { uri_record, text_record };
        NdefMessage sp_message = new NdefMessage(sp_records);

        NdefRecord sp_record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_SMART_POSTER, new byte[0],
                sp_message.toByteArray());

        NdefRecord aap_record = NdefRecord.createApplicationRecord(main
                .getPackageName());

        NdefRecord[] records = { sp_record, aap_record };
        NdefMessage message = new NdefMessage(records);
        return message;

    }

    /**
     * Creates a NDEF Text Message
     *
     * @param text
     *            Text to write
     * @return NDEF Message
     * @throws UnsupportedEncodingException
     */
    private NdefMessage createNdefTextMessage(String text)
            throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], payload);

        NdefRecord[] records = { record };
        NdefMessage message = new NdefMessage(records);

        return message;
    }

    /**
     * Creates a NDEF Uri Message
     *
     * @param uri
     *            Uri to write
     * @return NDEF Message
     */
    private NdefMessage createNdefUriMessage(String uri) {
        NdefRecord record = NdefRecord.createUri(uri);

        NdefRecord[] records = { record };
        NdefMessage message = new NdefMessage(records);

        return message;
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();

        byte[] data = new byte[len / 2];
        for (int i = 0, j = (len - 2) / 2; i < len; i += 2, j--) {
            data[j] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Closes the Reader, Reads the NDEF Message via Android function and
     * reconnects the Reader
     *
     * @param msg
     *            NDEF Message
     *
     * @return Message Type
     */
    private String getNdefMessageType(NdefMessage msg) {
        String type = "";

        NdefRecord records[] = msg.getRecords();

        if (records[0].getTnf() == NdefRecord.TNF_WELL_KNOWN) {
            if (Arrays.equals(records[0].getType(), NdefRecord.RTD_TEXT)) {
               // type = main.getResources().getString(R.string.radio_text);
            } else if (Arrays.equals(records[0].getType(), NdefRecord.RTD_URI)) {
              //  type = main.getResources().getString(R.string.radio_uri);
            } else if (Arrays.equals(records[0].getType(),
                    NdefRecord.RTD_HANDOVER_SELECT)) {
                // Now check that this is a BSSP Handover Select
                if (records[1].getTnf() == NdefRecord.TNF_MIME_MEDIA) {
                    if (Arrays.equals(records[1].getType(),
                            "application/vnd.bluetooth.ep.oob".getBytes())) {
                      //  type = main.getResources().getString(
                          //      R.string.radio_btpair);
                    }
                }
            } else if (Arrays.equals(records[0].getType() , NdefRecord.RTD_SMART_POSTER))
            {
                type= "Smartposter Record";
                for (NdefRecord record : records)
                {
                    //if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN & record.getType() == NdefRecord.)
                }
            }
        }

        return type;
    }

    /**
     * Reads the NDEF Message Text
     *
     * @param NDEF
     *            Message
     *
     * @return NDEF Message Text
     * @throws UnsupportedEncodingException
     */
    private String NDEFReadText(NdefMessage msg)
            throws UnsupportedEncodingException {
        String NDEFText = "";

        NDEFText = new String(msg.getRecords()[0].getPayload(), "US-ASCII");
        NDEFText = NDEFText.subSequence(3, NDEFText.length()).toString();

        return NDEFText;
    }

    /**
     * Reads the NDEF Message URI
     *
     * @param NDEF
     *            Message
     *
     * @return NDEF Message URI
     * @throws UnsupportedEncodingException
     */
    private String NDEFReadURI(NdefMessage msg)
            throws UnsupportedEncodingException {
        String NDEFText = "";

        NDEFText = new String(msg.getRecords()[0].getPayload(), 1,
                msg.getRecords()[0].getPayload().length - 1, "US-ASCII");
        NDEFText = getUriType(msg.getRecords()[0].getPayload()[0]).concat(
                NDEFText);

        return NDEFText;
    }

    /**
     * Reads the NDEF Message Bluetooth Secure Simple Pairing
     *
     * @param NDEF
     *            Message
     *
     * @return NDEF Message BSSP
     * @throws UnsupportedEncodingException
     */
    private String NDEFReadBSSP(NdefMessage msg)
            throws UnsupportedEncodingException {
        String NDEFText = "";

        byte[] payload = msg.getRecords()[1].getPayload();

        // First Field to parse: MAC Address;
        NDEFText = NDEFText.concat("MAC Address: ");
        for (int i = 7; i >= 2; i--) {
            NDEFText = NDEFText.concat("00".substring(Integer.toHexString(
                    payload[i] & 0xFF).length())
                    + Integer.toHexString(payload[i] & 0xFF).toUpperCase(
                    Locale.getDefault()));

            if (i != 2)
                NDEFText = NDEFText.concat(":");
            else
                NDEFText = NDEFText.concat("\n");
        }

        int fieldLength = 0;
        int fieldPosition = 8;

        do {
            fieldLength = payload[fieldPosition];

            if (payload[fieldPosition + 1] == 0x09) {
                NDEFText = NDEFText.concat("Device Name: ");
                NDEFText = NDEFText.concat(new String(payload,
                        fieldPosition + 2, fieldLength) + "\n");
            } else if (payload[fieldPosition + 1] == 0x0D) {
                NDEFText = NDEFText.concat("Service Class: ");
                for (int i = fieldPosition + 4; i >= fieldPosition + 2; i--) {
                    NDEFText = NDEFText.concat("00".substring(Integer
                            .toHexString(payload[i] & 0xFF).length())
                            + Integer.toHexString(payload[i] & 0xFF)
                            .toUpperCase(Locale.getDefault()));

                    if (i != fieldPosition + 2)
                        NDEFText = NDEFText.concat(":");
                    else
                        NDEFText = NDEFText.concat("\n");
                }
            }

            fieldPosition = fieldPosition + fieldLength + 1;
        } while (fieldPosition < payload.length);

        return NDEFText;
    }

    /**
     * This is a mapping of "URI Identifier Codes" to URI string prefixes, per
     * section 3.2.2 of the NFC Forum URI Record Type Definition document.
     */
    private static String getUriType(byte uriType) {
        switch (uriType) {
            case 0x01:
                return ("http://www.");
            case 0x02:
                return ("https://www.");
            case 0x03:
                return ("http://");
            case 0x04:
                return ("https://");
            case 0x05:
                return ("tel:");
            case 0x06:
                return ("mailto:");
            case 0x07:
                return ("ftp://anonymous:anonymous@");
            case 0x08:
                return ("ftp://ftp.");
            case 0x09:
                return ("ftps://");
            case 0x0A:
                return ("sftp://");
            case 0x0B:
                return ("smb://");
            case 0x0C:
                return ("nfs://");
            case 0x0D:
                return ("ftp://");
            case 0x0E:
                return ("dav://");
            case 0x0F:
                return ("news:");
            case 0x10:
                return ("telnet://");
            case 0x11:
                return ("imap:");
            case 0x12:
                return ("rtsp://");
            case 0x13:
                return ("urn:");
            case 0x14:
                return ("pop:");
            case 0x15:
                return ("sip:");
            case 0x16:
                return ("sips:");
            case 0x17:
                return ("tftp:");
            case 0x18:
                return ("btspp://");
            case 0x19:
                return ("btl2cap://");
            case 0x1A:
                return ("btgoep://");
            case 0x1B:
                return ("tcpobex://");
            case 0x1C:
                return ("irdaobex://");
            case 0x1D:
                return ("file://");
            case 0x1E:
                return ("urn:epc:id:");
            case 0x1F:
                return ("urn:epc:tag:");
            case 0x20:
                return ("urn:epc:pat:");
            case 0x21:
                return ("urn:epc:raw:");
            case 0x22:
                return ("urn:epc:");
            case 0x23:
                return ("urn:epc:raw:");
            default:
                return "";
        }
    }

    /**
     * Closes the Reader, Writes the NDEF Message via Android function and
     * reconnects the Reader
     *
     * @return NDEF Message
     * @throws IOException
     * @throws FormatException
     */
    private void NDEFWrite(NdefMessage msg) throws IOException, FormatException {
        reader.close();
        Ndef ndef = Ndef.get(tag);

        if (ndef != null) {
            ndef.connect();
            ndef.writeNdefMessage(msg);
            ndef.close();
        }
        reader.connect();
    }


    /*
     * Rounds the voltage to one single decimal
     */
    public double round(double value) {
        return Math.rint(value * 10) / 10;
    }

    /*
     * Helper function to show messages on the screen
     *
     * @param temp Message
     */
    protected void showResultDialog(String title) {
        new AlertDialog.Builder(main).setTitle(title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /*
     * Helper function to show messages on the screen
     *
     * @param temp Message
     */
    protected void showResultDialogMessage(String msg) {
        new AlertDialog.Builder(main).setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /**
     * Creates a NDEF Message
     *
     * @param text
     *            Text to write
     * @return NDEF Message
     * @throws UnsupportedEncodingException
     */
    private NdefMessage createNdefMessage(String text)
            throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], payload);

        NdefRecord[] records = { record };
        NdefMessage message = new NdefMessage(records);

        return message;
    }

    protected byte[] concat(byte[] one, byte[] two) {
        if (one == null)
            one = new byte[0];
        if (two == null)
            two = new byte[0];

        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);

        return combined;
    }
}
