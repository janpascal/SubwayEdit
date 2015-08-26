package org.vanbest.subwayedit;
/*
Inspiration: http://www.se7ensins.com/forums/threads/subway-surfers-hash-fixer.1259240/

 */
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jan-Pascal on 23-8-2015.
 * File format of playerdata file:
 * Offset Size Content
 * 0000   0004 Hash size (0x0014)
 * 0004   0014 SHA1 hash of salt + file after offset 0x001C
 * 0018   0004 Length of rest of file (filesize - 0x001C)
 * 001C   0004 0x0001 (maybe file version?)
 * 0020   0004 Number of key/value strings
 * ...
 *        0004 Number of key/number pairs
 * ...
 *        0004 Number of key/number pairs (second set)
 * ....
 */
public class SubwayFile {
    private String filename;
    private List<Entry> entries = null;
    private List<NumberEntry> firstNumberEntries = null;
    private List<NumberEntry> secondNumberEntries = null;

    private int fileVersion;

    private static final byte[] toprun_salt = { 0x47, 0x68, 0x67, 0x74, 0x72, 0x52, 0x46, 0x52, 0x66, 0x50, 0x4C, 0x4A, 0x68, 0x46, 0x44, 0x73, 0x57, 0x65, 0x23, 0x64, 0x45, 0x64, 0x72, 0x74, 0x35, 0x72, 0x66, 0x67, 0x35, 0x36 };
    private static final byte[] onlinesettings_salt = { 0x70, 0x64, 0x76, 0x73, 0x68, 0x62, 0x68, 0x6B, 0x6E, 0x64, 0x66, 0x39, 0x32, 0x6B, 0x31, 0x39, 0x7A, 0x76, 0x62, 0x63, 0x6B, 0x61, 0x77, 0x64, 0x39, 0x32, 0x66, 0x6A, 0x6B };
    private static final byte[] playerinfo_salt = { 0x77, 0x65, 0x31, 0x32, 0x72, 0x74, 0x79, 0x75, 0x69, 0x6B, 0x6C, 0x68, 0x67, 0x66, 0x64, 0x6A, 0x65, 0x72, 0x4B, 0x4A, 0x47, 0x48, 0x66, 0x76, 0x67, 0x68, 0x79, 0x75, 0x68, 0x6E, 0x6A, 0x69, 0x6F, 0x6B, 0x4C, 0x4A, 0x48, 0x6C, 0x31, 0x34, 0x35, 0x72, 0x74, 0x79, 0x66, 0x67, 0x68, 0x6A, 0x76, 0x62, 0x6E };

    private class Entry
    {
        String key;
        String value;
        public Entry(String key, String value) {
            this.key =key;
            this.value = value;
        }
    }
    private class NumberEntry
    {
        String key;
        int value;
        public NumberEntry(String key, int value) {
            this.key =key;
            this.value = value;
        }
    }


    private int readDword(BufferedInputStream in) throws IOException
    {
        return in.read() + in.read() * (1<<8) + in.read() * (1<<16) + in.read() * (1<<24);
    }

    private void writeDword(RandomAccessFile out, int dword) throws IOException
    {
        out.write(dword & 0x000000ff);
        out.write((dword & 0x0000ff00) >> 8);
        out.write((dword & 0x00ff0000) >> 16);
        out.write((dword & 0xff000000) >> 24);
    }

    private String readString(BufferedInputStream in) throws IOException
    {
        int count = in.read();
        if (count >= 0x80) {
            count = (count-0x80) + 0x80 * in.read();
        }
        byte[] chars = new byte[count];
        in.read(chars, 0, count);
        return new String(chars, "ASCII");
    }

    private void writeString(RandomAccessFile out, String s) throws IOException
    {
        byte[] bytes = s.getBytes("ASCII");
        int count = bytes.length;
        if (count < 0x80) {
            out.write(count);
        } else {
            out.write((count & 0x7f) + 0x80);
            out.write(count >> 7);
        }
        out.write(bytes);
    }

    protected void readFile(String filename) throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

        int hashSize = readDword(in);
        Log.d("SubwayFile", "Hash size: " + hashSize);
        byte[] hash = new byte[hashSize];
        in.read(hash, 0, hashSize);

        int fileSize = readDword(in);
        Log.d("SubwayFile", "File size: " + fileSize);
        fileVersion = readDword(in);
        Log.d("SubwayFile", "file version: " + fileVersion);

        int numStrings = readDword(in);
        Log.d("SubwayFile", "numStrings: " + numStrings);
        for(int i=0; i<numStrings && in.available()>0; i++) {
            String key = readString(in);
            String value = readString(in);
            entries.add(new Entry(key, value));
            Log.d("SubwayFile", "Read " + key + ": " + value);
        }
        int numNumbers = readDword(in);
        Log.d("SubwayFile", "numNumbers (first set): " + numNumbers);
        for(int i=0; i<numNumbers && in.available()>0; i++) {
            String key = readString(in);
            int value = readDword(in);
            firstNumberEntries.add(new NumberEntry(key, value));
            Log.d("SubwayFile", "Read number " + key + ": " + value);
        }
        numNumbers = readDword(in);
        Log.d("SubwayFile", "numNumbers (second set): " + numNumbers);
        for(int i=0; i<numNumbers && in.available()>0; i++) {
            String key = readString(in);
            int value = readDword(in);
            secondNumberEntries.add(new NumberEntry(key, value));
            Log.d("SubwayFile", "Read number " + key + ": " + value);
        }
    }

    protected void writeFile(String filename) throws IOException
    {
        Log.d("SubwayFile", "Writing subway file to " + filename);
        RandomAccessFile out = new RandomAccessFile(filename, "rw");

        MessageDigest SHA1;
        try {
            SHA1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return;
        }
        SHA1.reset();

        int hashSize = SHA1.getDigestLength();
        writeDword(out, hashSize);
        Log.d("SubwayFile", "Hash size: " + hashSize);

        long hashOffset = out.getFilePointer();
        // For now, write empty hash
        for(int i=0; i<hashSize; i++) out.write(0);

        //For now, write empty file size
        long sizeOffset = out.getFilePointer();
        writeDword(out, 0);

        long hashStartOffset = out.getFilePointer();

        writeDword(out, fileVersion);
        Log.d("SubwayFile", "file version: " + fileVersion);

        int numStrings = entries.size();
        writeDword(out, numStrings);
        Log.d("SubwayFile", "numStrings: " + numStrings);
        for(Entry entry: entries) {
            writeString(out, entry.key);
            writeString(out, entry.value);
        }

        int numNumbers = firstNumberEntries.size();
        writeDword(out, numNumbers);
        for(NumberEntry entry: firstNumberEntries) {
            writeString(out, entry.key);
            writeDword(out, entry.value);
        }
        numNumbers = secondNumberEntries.size();
        writeDword(out, numNumbers);
        for(NumberEntry entry: secondNumberEntries) {
            writeString(out, entry.key);
            writeDword(out, entry.value);
        }

        int fileSize = (int) (out.getFilePointer() - hashStartOffset);
        out.seek(sizeOffset);
        writeDword(out, fileSize);

        byte[] data = new byte[(int) fileSize];
        out.seek(hashStartOffset);
        out.read(data);
        SHA1.update(playerinfo_salt);
        SHA1.update(data);

        out.seek(hashOffset);
        out.write(SHA1.digest());

        out.close();
    }

    public SubwayFile()
    {
        this.filename = "none";
        entries = new ArrayList<Entry>();
        firstNumberEntries = new ArrayList<>();
        secondNumberEntries = new ArrayList<>();
    }

    public SubwayFile(String filename) throws IOException
    {
        this.filename = filename;
        entries = new ArrayList<Entry>();
        firstNumberEntries = new ArrayList<>();
        secondNumberEntries = new ArrayList<>();
        readFile(filename);
    }

    public String getKey(int i) {
        return entries.get(i).key;
    }

    public String getValue(int i) {
        return entries.get(i).value;
    }
    public void setValue(int i, String value) {
        entries.get(i).value = value;
    }
    public int getSize() {
        return entries.size();
    }
}
