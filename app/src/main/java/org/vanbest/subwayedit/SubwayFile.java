package org.vanbest.subwayedit;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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
    //unsigned char toprun_salt[] = { 0x47, 0x68, 0x67, 0x74, 0x72, 0x52, 0x46, 0x52, 0x66, 0x50, 0x4C, 0x4A, 0x68, 0x46, 0x44, 0x73, 0x57, 0x65, 0x23, 0x64, 0x45, 0x64, 0x72, 0x74, 0x35, 0x72, 0x66, 0x67, 0x35, 0x36 };
    //unsigned char onlinesettings_salt[] = { 0x70, 0x64, 0x76, 0x73, 0x68, 0x62, 0x68, 0x6B, 0x6E, 0x64, 0x66, 0x39, 0x32, 0x6B, 0x31, 0x39, 0x7A, 0x76, 0x62, 0x63, 0x6B, 0x61, 0x77, 0x64, 0x39, 0x32, 0x66, 0x6A, 0x6B };
    //unsigned char playerinfo_salt[] = { 0x77, 0x65, 0x31, 0x32, 0x72, 0x74, 0x79, 0x75, 0x69, 0x6B, 0x6C, 0x68, 0x67, 0x66, 0x64, 0x6A, 0x65, 0x72, 0x4B, 0x4A, 0x47, 0x48, 0x66, 0x76, 0x67, 0x68, 0x79, 0x75, 0x68, 0x6E, 0x6A, 0x69, 0x6F, 0x6B, 0x4C, 0x4A, 0x48, 0x6C, 0x31, 0x34, 0x35, 0x72, 0x74, 0x79, 0x66, 0x67, 0x68, 0x6A, 0x76, 0x62, 0x6E };

    private class Entry
    {
        String key;
        String value;
        public Entry(String key, String value) {
            this.key =key;
            this.value = value;
        }
    }

    private int readDword(BufferedInputStream in) throws IOException
    {
        return in.read() + in.read() * (1>>8) + in.read() * (1>>16) + in.read() * (1>>24);
    }

    private void writeDword(BufferedOutputStream out, int dword) throws IOException
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

    private void writeString(BufferedOutputStream out, String s) throws IOException
    {
        byte[] bytes = s.getBytes("ASCII");
        int count = bytes.length;
        if (count < 0x80) {
            out.write(count);
        } else {
            out.write(count & 0x7f + 0x80);
            out.write(count << 7);
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
        int fileVersion = readDword(in);
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
            //entries.add(new Entry(key, value));
            Log.d("SubwayFile", "Read number " + key + ": " + value);
        }
        numNumbers = readDword(in);
        Log.d("SubwayFile", "numNumbers (second set): " + numNumbers);
        for(int i=0; i<numNumbers && in.available()>0; i++) {
            String key = readString(in);
            int value = readDword(in);
            //entries.add(new Entry(key, value));
            Log.d("SubwayFile", "Read number " + key + ": " + value);
        }
    }

    public SubwayFile()
    {
        this.filename = "none";
        entries = new ArrayList<Entry>();
    }

    public SubwayFile(String filename) throws IOException
    {
        this.filename = filename;
        entries = new ArrayList<Entry>();
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
/*
Inspiration: http://www.se7ensins.com/forums/threads/subway-surfers-hash-fixer.1259240/
#include <stdio.h>
#include <stdlib.h>
#include <tomcrypt.h>
#include "endianio.h"
#include "MiscFunctions.h"

int salt_size;
unsigned char *saltPtr;
unsigned char toprun_salt[] = { 0x47, 0x68, 0x67, 0x74, 0x72, 0x52, 0x46, 0x52, 0x66, 0x50, 0x4C, 0x4A, 0x68, 0x46, 0x44, 0x73, 0x57, 0x65, 0x23, 0x64, 0x45, 0x64, 0x72, 0x74, 0x35, 0x72, 0x66, 0x67, 0x35, 0x36 };
unsigned char onlinesettings_salt[] = { 0x70, 0x64, 0x76, 0x73, 0x68, 0x62, 0x68, 0x6B, 0x6E, 0x64, 0x66, 0x39, 0x32, 0x6B, 0x31, 0x39, 0x7A, 0x76, 0x62, 0x63, 0x6B, 0x61, 0x77, 0x64, 0x39, 0x32, 0x66, 0x6A, 0x6B };
unsigned char playerinfo_salt[] = { 0x77, 0x65, 0x31, 0x32, 0x72, 0x74, 0x79, 0x75, 0x69, 0x6B, 0x6C, 0x68, 0x67, 0x66, 0x64, 0x6A, 0x65, 0x72, 0x4B, 0x4A, 0x47, 0x48, 0x66, 0x76, 0x67, 0x68, 0x79, 0x75, 0x68, 0x6E, 0x6A, 0x69, 0x6F, 0x6B, 0x4C, 0x4A, 0x48, 0x6C, 0x31, 0x34, 0x35, 0x72, 0x74, 0x79, 0x66, 0x67, 0x68, 0x6A, 0x76, 0x62, 0x6E };
void main()
{
    int i;
    int type;
    int length;
    char file[0x200];
    hash_state sha1;
    unsigned char o_hash[0x14];
    unsigned char n_hash[0x14];
    unsigned char *buffer;
    printf("Please drag a Subway Surfers save to this console:\r\n");
    scanf("%s", file);
    printf("\r\nWhat type of save is it (0 = TopRun, 1 = OnlineSettings, 2 = PlayerInfo)?\r\n");
    scanf("%i", &type);
    if (type == 0) { salt_size = 0x1D, saltPtr = toprun_salt; }
    else if (type == 1) { salt_size = 0x1E, saltPtr = onlinesettings_salt; }
    else if (type == 2) { salt_size = 0x33, saltPtr = playerinfo_salt; }
    EndianIO *eio = init_io(file, IO_OPENFILE);
    seek(eio, 0x4, SEEK_SET);
    memcpy(o_hash, readbytes(eio, 0x14), 0x14);
    eio->endian = LittleEndian;
    length = eio->length - 0x1C;
    writeint32(eio, length);
    eio->endian = BigEndian;
    buffer = malloc(length + salt_size);
    memcpy(buffer, saltPtr, salt_size);
    memcpy(buffer + salt_size, readbytes(eio, length), length);
    register_hash(&sha1_desc);
    sha1_init(&sha1);
    sha1_process(&sha1, buffer, length + salt_size);
    sha1_done(&sha1, n_hash);
    printf("\r\nOld Hash: 0x");
    for (i = 0; i < 0x14; i++)
    {
        printf("%X", o_hash[i]);
    }
    printf("\r\n");
    printf("New Hash: 0x");
    for (i = 0; i < 0x14; i++)
    {
        printf("%X", n_hash[i]);
    }
    printf("\r\n");
    seek(eio, 0x4, SEEK_SET);
    writebytes(eio, n_hash, 0x14);
    flush(eio);
    close(eio);
    free(buffer);
    printf("\r\nFinished!");
    getchar();
    getchar();
}
 */