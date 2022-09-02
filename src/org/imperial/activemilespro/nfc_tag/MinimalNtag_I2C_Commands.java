package org.imperial.activemilespro.nfc_tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Arrays;

import android.content.res.Configuration;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import org.imperial.activemilespro.nfc_tag.CC_differException;
import org.imperial.activemilespro.nfc_tag.DemoNotSupportedException;
import org.imperial.activemilespro.nfc_tag.DynamicLockBitsException;
import org.imperial.activemilespro.nfc_tag.StaticLockBitsException;
import org.imperial.activemilespro.nfc_tag.Ntag_Get_Version.Prod;
import org.imperial.activemilespro.nfc_tag.Ntag_I2C_Commands.Register;

/**
 * Class specific for the functions of The NTAG I2C
 *
 * @author NXP67729
 *
 */
public class MinimalNtag_I2C_Commands extends I2C_Enabled_Commands {

    MifareUltralight mfu;
    byte[] answer;
    static int wait_time = 20;

    /**
     * Special Registers of the NTAG I2C
     *
     */
    public enum Register {
        Session((byte) 0xF8), Configuration((byte) 0xE8), SRAM_Begin(
                (byte) 0xF0), User_memory_Begin((byte) 0x04), UID((byte) 0x00);

        byte value;

        private Register(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    // ---------------------------------------------------------------------------------
    // Begin Public Functions
    // ---------------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tag
     *            Tag to connect
     * @throws IOException
     */
    public MinimalNtag_I2C_Commands(Tag tag) throws IOException {
        BlockSize = 4;
        SRAMSize = 64;
        this.mfu = MifareUltralight.get(tag);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#close()
     */
    @Override
    public void close() throws IOException {
        mfu.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#connect()
     */
    @Override
    public void connect() throws IOException {
        mfu.connect();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#isConnected()
     */
    @Override
    public boolean isConnected() {
        return mfu.isConnected();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getLastAnswer()
     */
    @Override
    public byte[] getLastAnswer() {
        return answer;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getProduct()
     */
    @Override
    public Prod getProduct() throws IOException {
        // returns generic NTAG_I2C_1k, because getVersion is not possible
        return Prod.NTAG_I2C_1k;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getSessionRegisters()
     */
    @Override
    public byte[] getSessionRegisters() throws IOException, FormatException, DemoNotSupportedException {
        // returns a empty Byte array because accessing the Session Registers is
        // not possible with this minimal implementation
        throw  new DemoNotSupportedException("getSessionRegisters not supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getConfigRegisters()
     */
    @Override
    public byte[] getConfigRegisters() throws IOException, FormatException, DemoNotSupportedException {
        answer = mfu.readPages(0xE8);
        return answer;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getConfigRegister(com.nxp
     * .nfc_demo.reader.Ntag_I2C_Commands.CR_Offset)
     */
    @Override
    public byte getConfigRegister(CR_Offset off) throws IOException,
            FormatException, DemoNotSupportedException {
        return getConfigRegisters()[off.getValue()];
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getSessionRegister(com.nxp
     * .nfc_demo.reader.Ntag_I2C_Commands.SR_Offset)
     */
    @Override
    public byte getSessionRegister(SR_Offset off) throws IOException,
            FormatException, DemoNotSupportedException {
        throw  new DemoNotSupportedException("getConfigRegister not supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeConfigRegisters(byte,
     * byte, byte, byte, byte, byte)
     */
    @Override
    public void writeConfigRegisters(byte NC_R, byte LD_R, byte SM_R,
                                     byte WD_LS_R, byte WD_MS_R, byte I2C_CLOCK_STR) throws IOException,
            FormatException {

        byte[] Data = new byte[4];

        // Write the Config Regs
        Data[0] = NC_R;
        Data[1] = LD_R;
        Data[2] = SM_R;
        Data[3] = WD_LS_R;
        mfu.writePage(0xE8, Data);

        Data[0] = WD_MS_R;
        Data[1] = I2C_CLOCK_STR;
        Data[2] = (byte) 0x00;
        Data[3] = (byte) 0x00;
        mfu.writePage(0xE9, Data);

    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#waitforI2Cwrite()
     */
    @Override
    public void waitforI2Cwrite(int timeoutMS) throws IOException, FormatException {
        // just wait a little

        try {
            Thread.sleep(wait_time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#waitforI2Cread()
     */
    @Override
    public void waitforI2Cread(int timeoutMS) throws IOException, FormatException {
        // just wait a little
        try {
            Thread.sleep(wait_time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEEPROM(byte[])
     */
    @Override
    public void writeEEPROM(byte[] data) throws IOException, FormatException {
        if(data.length > getProduct().getMemsize())
        {
            throw new IOException("Data is to long");
        }

        byte[] temp;
        int Index = 0;
        byte BlockNr = Register.User_memory_Begin.getValue();

        // write till all Data is written
        for (Index = 0; Index < data.length; Index += 4) {
            temp = Arrays.copyOfRange(data, Index, Index + 4);
            mfu.writePage(BlockNr, temp);
            BlockNr++;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEEPROM(int,
     * byte[])
     */
    @Override
    public void writeEEPROM(int startAddr, byte[] data) throws IOException,
            FormatException {
        // Nothing will be done for now
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readEEPROM(int, int)
     */
    @Override
    public byte[] readEEPROM(int absStart, int absEnd) throws IOException,
            FormatException {
        byte[] temp = new byte[0];
        answer = new byte[0];

        if (absStart > 0xFF)
            absStart = 0xFF;

        if (absEnd > 0xFF)
            absEnd = 0xFF;

        int i;
        for (i = absStart; i <= (absEnd - 3); i += 4) {
            temp = mfu.readPages(i);
            answer = concat(answer, temp);
        }

        if (i < absEnd) {
            temp = mfu.readPages(absEnd - 3);
            byte[] bla = Arrays.copyOfRange(temp, (i - (absEnd - 3))*4, 16);
            answer = concat(answer, bla);
        }
        return answer;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeSRAMBlock(byte[])
     */
    @Override
    public void writeSRAMBlock(byte[] data) throws IOException, FormatException {
        byte[] TxBuffer = new byte[4];
        int index = 0;

        for (int i = 0; i < 16; i++) {
            for (int d_i = 0; d_i < 4; d_i++) {
                if (index < data.length)
                    TxBuffer[d_i] = data[index++];
                else
                    TxBuffer[d_i] = (byte) 0x00;
            }
            mfu.writePage(0xF0 + i, TxBuffer);

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeSRAM(byte[],
     * com.nxp.nfc_demo.reader.Ntag_I2C_Commands.R_W_Methods)
     *
     * @throws InterruptedException
     */
    @Override
    public void writeSRAM(byte[] data, R_W_Methods method) throws IOException,
            FormatException {

        int Blocks = (int) Math.ceil(data.length / 64.0);
        for (int i = 0; i < Blocks; i++) {

            writeSRAMBlock(data);
            if (method == R_W_Methods.Polling_Mode) {
                waitforI2Cread(100);
            } else {
                try {
                    // else wait
                    Thread.sleep(6);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (data.length > 64)
                data = Arrays.copyOfRange(data, 64, data.length);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readSRAMBlock()
     */
    @Override
    public byte[] readSRAMBlock() throws IOException, FormatException {

        answer = new byte[0];
        for (int i = 0; i < 0x0F; i += 4)
            answer = concat(answer, mfu.readPages(0xF0 + i));

        return answer;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readSRAM(int,
     * com.nxp.nfc_demo.reader.Ntag_I2C_Commands.R_W_Methods)
     */
    @Override
    public byte[] readSRAM(int blocks, R_W_Methods method) throws IOException,
            FormatException {
        byte[] response = new byte[0];
        byte[] temp;
        answer = new byte[0];

        for (int i = 0; i < blocks; i++) {
            if (method == R_W_Methods.Polling_Mode) {
                waitforI2Cwrite(100);
            } else {
                try {
                    // else wait
                    Thread.sleep(6);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            temp = readSRAMBlock();

            // concat read block to the full response
            response = concat(response, temp);
        }
        answer = response;
        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEmptyNdef()
     */
    @Override
    public void writeEmptyNdef() throws IOException, FormatException {
        // Nothing done for now
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeDeliveryNdef()
     */
    @Override
    public void writeDeliveryNdef() throws IOException, FormatException,
            CC_differException, StaticLockBitsException,
            DynamicLockBitsException {
        // Nothing done for now
        return;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeNDEF()
     */
    @Override
    public void writeNDEF(NdefMessage message) throws IOException,
            FormatException {
        byte[] Ndef_message_byte = createRawNdefTlv(message);
        writeEEPROM(Ndef_message_byte);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readNDEF()
     */
    @Override
    public NdefMessage readNDEF() throws IOException, FormatException {
        int NDEFsize;
        int TLVsize;
        int TLV_plus_NDEF;

        // get TLV
        byte[] TLV = readEEPROM(Register.User_memory_Begin.getValue(),
                Register.User_memory_Begin.getValue() + 3);

        // checking TLV - maybe there are other TLVs on the tag
        if (TLV[0] != 0x03) {
            throw new FormatException("Format on Tag not supported");
        }

        if (TLV[1] != (byte) 0xFF) {
            NDEFsize = (TLV[1] & 0xFF);
            TLVsize = 2;
            TLV_plus_NDEF = TLVsize + NDEFsize;
        } else {
            NDEFsize = (TLV[3] & 0xFF);
            NDEFsize |= ((TLV[2] << 8) & 0xFF00);
            TLVsize = 4;
            TLV_plus_NDEF = TLVsize + NDEFsize;
        }

        // Read NDEF Message
        byte[] data = readEEPROM(Register.User_memory_Begin.getValue(),
                Register.User_memory_Begin.getValue() + (TLV_plus_NDEF / 4));

        // delete TLV
        data = Arrays.copyOfRange(data, TLVsize, data.length);
        // delete end of String which is not part of the NDEF Message
        data = Arrays.copyOf(data, NDEFsize);

        // get the String out of the Message
        NdefMessage message = new NdefMessage(data);
        return message;
    }

    // -------------------------------------------------------------------
    // Helping function
    // -------------------------------------------------------------------

    /**
     * create a Raw NDEF TLV from a NDEF Message
     *
     * @param NdefMessage
     *            NDEF Message to put in the NDEF TLV
     * @return Byte Array of NDEF Message
     * @throws UnsupportedEncodingException
     */
    private byte[] createRawNdefTlv(NdefMessage NDEFmessage)
            throws UnsupportedEncodingException {
        // creating NDEF
        byte[] Ndef_message_byte = NDEFmessage.toByteArray();
        int ndef_message_size = Ndef_message_byte.length;
        byte[] message;

        if (ndef_message_size < 0xFF) {
            message = new byte[ndef_message_size + 3];
            byte TLV_size = 0;
            TLV_size = (byte) ndef_message_size;
            message[0] = (byte) 0x03;
            message[1] = (byte) TLV_size;
            message[message.length - 1] = (byte) 0xFE;
            System.arraycopy(Ndef_message_byte, 0, message, 2,
                    Ndef_message_byte.length);
        } else {
            message = new byte[ndef_message_size + 5];
            int TLV_size = ndef_message_size;
            TLV_size |= 0xFF0000;
            message[0] = (byte) 0x03;
            message[1] = (byte) ((TLV_size >> 16) & 0xFF);
            message[2] = (byte) ((TLV_size >> 8) & 0xFF);
            message[3] = (byte) (TLV_size & 0xFF);
            message[message.length - 1] = (byte) 0xFE;
            System.arraycopy(Ndef_message_byte, 0, message, 4,
                    Ndef_message_byte.length);
        }

        return message;
    }

    @Override
    public Boolean checkPTwritePossible() throws IOException, FormatException {
        // Just wait some time
        try {
            Thread.sleep(wait_time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
