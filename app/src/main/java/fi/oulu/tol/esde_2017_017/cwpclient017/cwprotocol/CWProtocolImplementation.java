package fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import android.nfc.Tag;
import android.os.Handler;
import android.support.compat.BuildConfig;
import android.text.Html;
import android.util.Log;

public class CWProtocolImplementation implements CWPControl, CWPMessaging, Runnable {
    public enum CWPState { Disconnected, Connected, LineUp, LineDown }
    private CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int frequency = DEFAULT_FREQUENCY;
    public static final int FORBIDDEN_FREQUENCY = -2147483648;
    private CWPConnectionReader connectionReader = null;
    private int messageValue;
    private Handler receiveHandler = new Handler();
    private CWProtocolListener listener = null;
    private static final int BUFFER_LENGTH = 64;
    private OutputStream networkOutputStream = null;
    private ByteBuffer outBuffer = null;
    private String serverAddr = null;
    private int serverPort = -1;
    private int sessionInitTime;
    private int lineUpTimeStamp;
    private boolean isLineUpByUser = false;
    private boolean isLineUpFromServer = false;
    private static final String TAG = "FROM MAIN: ";
    private Semaphore lock = new Semaphore(1);
    public CWProtocolImplementation (CWProtocolListener l) {
        listener = l;
    }

    public void addObserver(Observer observer) {}
    public void deleteObserver(Observer observer) {}
    public void connect(String address, int port, int freq) throws IOException {
        serverAddr = address;
        serverPort = port;
        setFrequency(freq);
        if (BuildConfig.DEBUG) throw new IllegalStateException("connect == null when disconnect called");
        connectionReader = new CWPConnectionReader(this);
        connectionReader.startReading();
    }
    public void disconnect() throws IOException {
        try {
            serverAddr = null;
            serverPort = -1;
            frequency = DEFAULT_FREQUENCY;
            connectionReader.stopReading();
            connectionReader = null;
        } catch (InterruptedException e) {
            Log.d("Disconnect", "IO Exception received from disconnect");
        }
    }
    public boolean isConnected() {
        return currentState != CWPState.Disconnected;
    }
    public void setFrequency(int f) throws IOException {
        if (frequency != f) {
            if (f > 0) {
                frequency = -f;
            } else if (f == 0) {
                frequency = DEFAULT_FREQUENCY;
            } else {
                frequency = f;
            }
            sendFrequency();
        }
    }
    public int getFrequency() {
        return frequency;
    }

    public void lineUp() throws IOException {
        boolean isStateChangedToLineUp = false;
        try {
            lock.acquire();
            if(!isLineUpByUser && (currentState == CWPState.LineUp || currentState == CWPState.LineDown)) {
                lineUpTimeStamp = (int)System.currentTimeMillis() - sessionInitTime;
                currentState = CWPState.LineUp;
                sendMessage(lineUpTimeStamp);
                if (currentState == CWPState.LineDown && !isLineUpFromServer) {
                    currentState = CWPState.LineUp;
                    isStateChangedToLineUp = true;
                }
                isLineUpByUser = true;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if (isStateChangedToLineUp) {
            listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
        }
    }

    public void lineDown() throws IOException {
        boolean isStateChangedToLineDown = false;
        short msg = 0;
        try {
            lock.acquire();
            if (currentState == CWPState.LineUp && isLineUpByUser) {
                msg = (short)(System.currentTimeMillis() - sessionInitTime - lineUpTimeStamp);
                sendMessage(msg);
                isLineUpByUser = false;
                if (!isLineUpFromServer) {
                    currentState = CWPState.LineDown;
                    isStateChangedToLineDown = true;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if (isStateChangedToLineDown) {
            listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
        }
    }

    public boolean lineIsUp() {
        return currentState == CWPState.LineUp;
    }

    private void sendMessage(int msg) throws IOException {
        if (msg != FORBIDDEN_FREQUENCY) {
            Log.d("Send", "Sending msg to server: " + msg);
            outBuffer = ByteBuffer.allocate(4);
            outBuffer.order(ByteOrder.BIG_ENDIAN);
            outBuffer.putInt(msg);
            outBuffer.position(0);
            final byte[] buf = outBuffer.array();
            networkOutputStream.write(buf);
            networkOutputStream.flush();
            outBuffer = null;
        }
    }
    private void sendMessage(short msg) throws IOException {
        if (msg != FORBIDDEN_FREQUENCY) {
            Log.d("Send", "Sending msg to server: " + msg);
            outBuffer = ByteBuffer.allocate(2);
            outBuffer.order(ByteOrder.BIG_ENDIAN);
            outBuffer.putShort(msg);
            outBuffer.position(0);
            final byte[] buf = outBuffer.array();
            networkOutputStream.write(buf);
            networkOutputStream.flush();
            outBuffer = null;
        }
    }

    private void sendFrequency() throws IOException {
        boolean isFrequencySent = false;
        try {
            lock.acquire();
            if (currentState == CWPState.LineDown) {
                try {
                    Log.d(TAG, "Sending frequency...");
                    sendMessage(frequency);
                    currentState = CWPState.Connected;
                    isFrequencySent = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "State is not Line Down! Set frequency later");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.release();
        }
        if (isFrequencySent) {
            listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
        }
    }

    public CWPState getCurrentState() {
        return currentState;
    }

    @Override
    public void run() {
        Log.d("Main Thread: ", "State is changing. New state: " + nextState);
        int tempMessageValue = messageValue;
        switch (nextState) {
            case Connected:
                currentState = nextState;
                lock.release();
                isLineUpFromServer = false;
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, tempMessageValue);
                break;
            case Disconnected:
                currentState = nextState;
                lock.release();
                isLineUpFromServer = false;
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, tempMessageValue);
                break;
            case LineUp:
                isLineUpFromServer = true;
                if (!isLineUpByUser) {
                    currentState = nextState;
                    listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, tempMessageValue);
                }
                lock.release();
                break;
            case LineDown:
                if (currentState == CWPState.Connected) {
                    currentState = nextState;
                    lock.release();
                    Log.d(TAG, "state changed from connected to line down");
                    if(frequency != tempMessageValue) {
                        try {
                            sendFrequency();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    } else {
                        listener.onEvent(CWProtocolListener.CWPEvent.EChangedFrequency, tempMessageValue);
                    }
                } else {
                    isLineUpFromServer = false;
                    if (!isLineUpByUser) {
                        currentState = nextState;
                        listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, tempMessageValue);
                    }
                    lock.release();
                }
                break;
        }
    }

    private class CWPConnectionReader extends Thread {
        private boolean isRunning = false;
        private Runnable myProcessor = null;
        private static final String TAG = "CWPReader";
        private Socket cwpSocket = null;
        private InputStream networkInputStream = null;

        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }
        void startReading() {
            isRunning = true;
            start();
        }

        void stopReading() throws InterruptedException, IOException {
            cwpSocket.close();
            networkOutputStream.close();
            networkInputStream.close();
            cwpSocket = null;
            isRunning = false;
            changeProtocolState(CWPState.Disconnected, 0);
        }
        private void doInitialize() throws InterruptedException, IOException {
            SocketAddress socketAddress = new InetSocketAddress(serverAddr, serverPort);
            cwpSocket = new Socket();
            cwpSocket.connect(socketAddress, 5000);
            sessionInitTime = (int)System.currentTimeMillis();
            networkInputStream = cwpSocket.getInputStream();
            networkOutputStream = cwpSocket.getOutputStream();
            isLineUpByUser = false;
            isLineUpFromServer = false;
            changeProtocolState(CWPState.Connected, 0);
        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException{
            Log.d(TAG, "Change protocol state to: " +  state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(myProcessor);
        }

        @Override
        public void run() {
            try {
                doInitialize();
                byte[] bytes = new byte[BUFFER_LENGTH];
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
                buffer.order(ByteOrder.BIG_ENDIAN);
                while(isRunning) {
                    handleIncomingMessages(bytes, buffer);  // todo: Figure out why this loops only once. Same issue appeared before refactoring.
                    Log.d(TAG, "IS RUNNING");
                }
            } catch (IOException e) {
                Log.d(TAG, "IO Exception received. Exception: " + e.toString());  // todo: add exception handling.
            } catch (InterruptedException e) {
                Log.d(TAG, "Interrupted Exception received: " + e.toString());  // todo: add exception handling.
            }
        }

        private void handleIncomingMessages(byte[] bytes, ByteBuffer buffer) throws IOException, InterruptedException {
            int bytesRead, bytesToRead = 4;
            bytesRead = getReadBytes(bytes, bytesToRead);
            if (bytesRead > 0) {
                clearAndPutBytesToBuffer(bytesRead, bytes, buffer);
                int serverMessageInt = buffer.getInt();
                if (serverMessageInt >= 0) {
                    receiveLineUpFromServer(bytes, buffer, serverMessageInt);
                } else if (serverMessageInt != FORBIDDEN_FREQUENCY) {
                    changeProtocolState(CWPState.LineDown, serverMessageInt);
                }
            }
            Log.d(TAG, "Incoming msg handled. Frequency: " + frequency);
        }

        private void receiveLineUpFromServer(byte[] bytes, ByteBuffer buffer, int serverMessageInt) throws InterruptedException, IOException {
            int bytesRead, bytesToRead = 2;
            changeProtocolState(CWPState.LineUp, serverMessageInt);
            bytesRead = getReadBytes(bytes, bytesToRead);
            if (bytesRead > 0) {
                clearAndPutBytesToBuffer(bytesRead, bytes, buffer);
                short serverMessageShort = buffer.getShort();
                changeProtocolState(CWPState.LineDown, serverMessageShort);
            }
            Log.d(TAG, "line up from server received");
        }

        private void clearAndPutBytesToBuffer(int bytesRead, byte[] bytes, ByteBuffer buffer) {
            buffer.clear();
            buffer.put(bytes, 0, bytesRead);
            buffer.position(0);
        }

        private int getReadBytes(byte [] bytes, int bytesToRead) throws IOException {
            int bytesRead = 0;
            do {
                Arrays.fill(bytes, (byte)0);
                int readNow = networkInputStream.read(bytes, bytesRead, bytesToRead - bytesRead);
                Log.d(TAG, "Byte count read " + readNow);
                if (readNow == -1){
                    throw new IOException("Read -1 from server");
                } else {
                    bytesRead += readNow;
                }
            } while (bytesRead < bytesToRead);
            return bytesRead;
        }
    }
}