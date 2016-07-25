package com.idevicesinc.sweetblue;


import com.idevicesinc.sweetblue.listeners.DeviceConnectionFailListener;
import com.idevicesinc.sweetblue.utils.BleStatuses;

public class P_Task_Connect extends P_Task_RequiresBleOn
{

    private final P_TaskPriority mPriority;
    private final boolean mExplicit;


    public P_Task_Connect(BleDevice device, IStateListener listener)
    {
        this(device, listener, true, null);
    }

    public P_Task_Connect(BleDevice device, IStateListener listener, boolean explicit)
    {
        this(device, listener, explicit, null);
    }

    public P_Task_Connect(BleDevice device, IStateListener listener, boolean explicit, P_TaskPriority priority)
    {
        super(device, listener);

        mPriority = priority == null ? P_TaskPriority.MEDIUM : priority;
        mExplicit = explicit;
    }

    @Override void checkTimeOut(long curTimeMs)
    {
        if (timeExecuting() >= getDevice().getConfig().connectTimeOut.millis())
        {
            getTaskManager().timeOut(this);
        }
    }

    @Override void onTaskTimedOut()
    {
        super.onTaskTimedOut();
//        getDevice().onConnectionFailed(DeviceConnectionFailListener.Status.NATIVE_CONNECTION_FAILED, DeviceConnectionFailListener.Timing.EVENTUALLY, BleStatuses.GATT_STATUS_NOT_APPLICABLE);
    }

    @Override public P_TaskPriority getPriority()
    {
        return mPriority;
    }

    @Override public void execute()
    {
        getDevice().doNativeConnect();
    }

}
