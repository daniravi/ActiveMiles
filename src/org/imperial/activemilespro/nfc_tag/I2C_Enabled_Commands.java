package org.imperial.activemilespro.nfc_tag;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.MifareUltralight;

import java.io.IOException;

import java.util.concurrent.TimeoutException;

import org.imperial.activemilespro.nfc_tag.Ntag_Get_Version.Prod;

import org.imperial.activemilespro.nfc_tag.CC_differException;
import org.imperial.activemilespro.nfc_tag.DemoNotSupportedException;
import org.imperial.activemilespro.nfc_tag.DynamicLockBitsException;
import org.imperial.activemilespro.nfc_tag.StaticLockBitsException;


public abstract class I2C_Enabled_Commands {

    public static I2C_Enabled_Commands get(Tag tag) throws IOException,
            InterruptedException {
        byte[] answer;
        byte[] command = new byte[2];

        NfcA nfca = NfcA.get(tag);
        Prod prod;

        // Check for support of setTimout to be able to send an efficient
        // sector_select - select minimal implementation if not supported
        nfca.setTimeout(20);
        // check for timeout
        if (nfca.getTimeout() < 50) {
            // check if GetVersion is supported
            try {
                nfca.connect();
                command = new byte[1];
                command[0] = (byte) 0x60;
                answer = nfca.transceive(command);
                prod = (new Ntag_Get_Version(answer)).Get_Product();
                nfca.close();
                if (prod == Prod.NTAG_I2C_1k || prod == Prod.NTAG_I2C_2k)
                    return new Ntag_I2C_Commands(tag);
            } catch (Exception e) {
                nfca.close();
                // check if sector_select is supported
                try {
                    nfca.connect();
                    command = new byte[2];
                    command[0] = (byte) 0xC2;
                    command[1] = (byte) 0xFF;
                    answer = nfca.transceive(command);

                    nfca.close();
                    return new Ntag_I2C_Commands(tag);
                } catch (Exception e2) {
                    nfca.close();
                }
            }
        }

        // check if we can use the minimal Version
        MifareUltralight mfu = MifareUltralight.get(tag);
        try {
            mfu.connect();
            answer = mfu.readPages(0);
            // no exception is thrown so the phone can use the mfu.readPages
            // function
            // also check if:
            // - tag is from NXP (byte 0 == 0x04)
            // - CC corresponds to a NTAG I2C 1K
            if (answer[0] == (byte) 0x04 && answer[12] == (byte) 0xE1
                    && answer[13] == (byte) 0x10 && answer[14] == (byte) 0x6D
                    && answer[15] == (byte) 0x00) {
                // check if Config is readable (distinguish from NTAG216), if
                // not exception is thrown, and tag is not an
                // NTAG I2C 1k
                answer = mfu.readPages(0xE8);
                mfu.close();
                return new MinimalNtag_I2C_Commands(tag);
            }
            mfu.close();
        } catch (Exception e) {
            e.printStackTrace();
            mfu.close();
        }

        return null;
    }

    protected int SRAMSize;

    public int getSRAMSize() {
        return SRAMSize;
    }

    protected int BlockSize;

    public int getBlockSize() {
        return BlockSize;
    }

    /**
     * Different Read Methods which are possible with the NTAG I2C
     *
     */
    public enum R_W_Methods {
        Fast_Mode, Polling_Mode, Error
    }

    /**
     * Bits of the NS_REG Register
     *
     */
    public enum NS_Reg_Func {
        RF_FIELD_PRESENT((byte) (0x01 << 0)), EEPROM_WR_BUSY((byte) (0x01 << 1)), EEPROM_WR_ERR(
                (byte) (0x01 << 2)), SRAM_RF_READY((byte) (0x01 << 3)), SRAM_I2C_READY(
                (byte) (0x01 << 4)), RF_LOCKED((byte) (0x01 << 5)), I2C_LOCKED(
                (byte) (0x01 << 6)), NDEF_DATA_READ((byte) (0x01 << 7)), ;

        byte value;

        private NS_Reg_Func(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Bits of the NC_REG Register
     *
     */
    public enum NC_Reg_Func {
        PTHRU_DIR((byte) (0x01 << 0)), SRAM_MIRROR_ON_OFF((byte) (0x01 << 1)), FD_ON(
                (byte) (0x03 << 2)), FD_OFF((byte) (0x03 << 4)), PTHRU_ON_OFF(
                (byte) (0x01 << 6)), I2C_RST_ON_OFF((byte) (0x01 << 7)), ;

        byte value;

        private NC_Reg_Func(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Offset of the Config Registers
     *
     */
    public enum CR_Offset {
        NC_REG((byte) 0x00), LAST_NDEF_PAGE((byte) 0x01), SM_REG((byte) 0x02), WDT_LS(
                (byte) 0x03), WDT_MS((byte) 0x04), I2C_CLOCK_STR((byte) 0x05), REG_LOCK(
                (byte) 0x06), FIXED((byte) 0x07);

        byte value;

        private CR_Offset(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Offset of the Session Registers
     *
     */
    public enum SR_Offset {
        NC_REG((byte) 0x00), LAST_NDEF_PAGE((byte) 0x01), SM_REG((byte) 0x02), WDT_LS(
                (byte) 0x03), WDT_MS((byte) 0x04), I2C_CLOCK_STR((byte) 0x05), NS_REG(
                (byte) 0x06), FIXED((byte) 0x07);

        byte value;

        private SR_Offset(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * Closes the connection
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;

    /**
     * reopens the connection
     *
     * @throws IOException
     */
    public abstract void connect() throws IOException;

    /**
     * reopens the connection
     *
     * @throws IOException
     */
    public abstract boolean isConnected();

    /**
     * returns the last answer as Byte Array
     *
     * @return Byte Array of the last Answer
     */
    public abstract byte[] getLastAnswer();

    /**
     * Gets the Product of the current Tag
     *
     * @return Product of the Tag
     * @throws IOException
     */
    public abstract Prod getProduct() throws IOException;

    /**
     * Gets all Session Registers as Byte Array
     *
     * @return all Session Registers
     * @throws IOException
     * @throws FormatException
     * @throws DemoNotSupportedException
     */
    public abstract byte[] getSessionRegisters() throws IOException,
            FormatException, DemoNotSupportedException;

    /**
     * Gets all Config Registers as Byte Array
     *
     * @return all Config Registers
     * @throws IOException
     * @throws FormatException
     * @throws DemoNotSupportedException
     */
    public abstract byte[] getConfigRegisters() throws IOException,
            FormatException, DemoNotSupportedException;

    /**
     * Gets a specific Config Register
     *
     * @param off
     *            Offset of the Config Register
     * @return Register
     * @throws IOException
     * @throws FormatException
     * @throws DemoNotSupportedException
     */
    public abstract byte getConfigRegister(CR_Offset off) throws IOException,
            FormatException, DemoNotSupportedException;

    /**
     * Gets a specific Session Register
     *
     * @param off
     *            Offset of the Session Register
     * @return Register
     * @throws IOException
     * @throws FormatException
     * @throws DemoNotSupportedException
     */
    public abstract byte getSessionRegister(SR_Offset off) throws IOException,
            FormatException, DemoNotSupportedException;

    /**
     * Writes the Config registers
     *
     * @param NC_R
     * @param LD_R
     * @param SM_R
     * @param WD_LS_R
     * @param WD_MS_R
     * @param I2C_CLOCK_STR
     * @throws IOException
     * @throws FormatException
     */
    public abstract void writeConfigRegisters(byte NC_R, byte LD_R, byte SM_R,
                                              byte WD_LS_R, byte WD_MS_R, byte I2C_CLOCK_STR) throws IOException,
            FormatException;

    /**
     * Checks if the Phone can write in the SRAM when PT is enabled
     *
     * @return
     * @throws FormatException
     * @throws IOException
     */
    public abstract Boolean checkPTwritePossible() throws IOException,
            FormatException;

    /**
     * Waits till the I2C has written in the SRAM
     * @param timeoutMS TODO
     *
     * @throws IOException
     * @throws FormatException
     * @throws TimeoutException
     */
    public abstract void waitforI2Cwrite(int timeoutMS) throws IOException, FormatException, TimeoutException;

    /**
     * Waits till the I2C has read the SRAM
     * @param timeoutMS TODO
     *
     * @throws IOException
     * @throws FormatException
     * @throws TimeoutException
     */
    public abstract void waitforI2Cread(int timeoutMS) throws IOException, FormatException, TimeoutException;

    /**
     * Writes Data to the EEPROM as long as enough space is on the Tag
     *
     * @param data
     *            Raw Data to write
     * @throws IOException
     * @throws FormatException
     */
    public abstract void writeEEPROM(byte[] data) throws IOException,
            FormatException;

    /**
     * Writes Data to the EEPROM as long as enough space is on the Tag
     *
     * @param data
     *            Raw Data to write
     * @param startAddr
     *            Start Address from which the write begins
     * @throws IOException
     * @throws FormatException
     */
    public abstract void writeEEPROM(int startAddr, byte[] data)
            throws IOException, FormatException;

    /**
     * Read Data from the EEPROM
     *
     * @param absStart
     *            Start of the read
     * @param absEnd
     *            End of the read(included in the Answer)
     * @return Data read
     * @throws IOException
     * @throws FormatException
     *
     */
    public abstract byte[] readEEPROM(int absStart, int absEnd)
            throws IOException, FormatException;

    /**
     * Writes on SRAM Block Only 64 Bytes are transfered
     *
     * @param data
     *            Data to write
     * @throws IOException
     * @throws FormatException
     */
    public abstract void writeSRAMBlock(byte[] data) throws IOException,
            FormatException;

    /**
     * Writes Data in the SRAM, when 64 Bytes exceeded more Blocks are written
     *
     * @param data
     *            Data to write
     * @throws IOException
     * @throws FormatException
     * @throws TimeoutException
     */
    public abstract void writeSRAM(byte[] data, R_W_Methods method)
            throws IOException, FormatException, TimeoutException;

    /**
     * Reads one SRAM Block
     *
     * @param Method
     *            Method with which the SRAM is read
     * @return Byte Array of the read
     * @throws IOException
     * @throws FormatException
     */
    public abstract byte[] readSRAMBlock() throws IOException, FormatException;

    /**
     * Reads as many Blocks as Specified
     *
     * @param blocks
     *            Blocks to read
     * @param method
     *            Method with which the SRAM is read
     * @return Returns the Byte Array of the Read
     * @throws IOException
     * @throws FormatException
     * @throws TimeoutException
     */
    public abstract byte[] readSRAM(int blocks, R_W_Methods method)
            throws IOException, FormatException, TimeoutException;

    /**
     * Write an Empty NDEF Message to the NTAG
     *
     * @throws IOException
     * @throws FormatException
     */
    public abstract void writeEmptyNdef() throws IOException, FormatException;

    /**
     * Resets the Tag, this includes: Capability Container and User Memory
     *
     * @throws IOException
     * @throws FormatException
     * @throws CC_differException
     * @throws StaticLockBitsException
     * @throws DynamicLockBitsException
     */
    public abstract void writeDeliveryNdef() throws IOException,
            FormatException, CC_differException, StaticLockBitsException,
            DynamicLockBitsException;

    /**
     * Read a NDEF Message from the tag - not an official NFC Forum NDEF
     * detection routine
     *
     * @param message
     *            NDEF message to write on the tag
     * @throws IOException
     * @throws FormatException
     */
    public abstract void writeNDEF(NdefMessage message) throws IOException,
            FormatException;

    /**
     * Read a NDEF Message from the tag - not an official NFC Forum NDEF
     * detection routine
     *
     * @throws IOException
     * @throws FormatException
     */
    public abstract NdefMessage readNDEF() throws IOException, FormatException;

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