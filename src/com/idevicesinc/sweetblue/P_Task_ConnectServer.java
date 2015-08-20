package com.idevicesinc.sweetblue;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;

class P_Task_ConnectServer extends PA_Task_ConnectOrDisconnectServer
{
	private BleServer.ConnectionFailListener.Status m_status = BleServer.ConnectionFailListener.Status.NULL;

	public P_Task_ConnectServer(BleServer server, BluetoothDevice nativeDevice, I_StateListener listener, boolean explicit, PE_TaskPriority priority)
	{
		super(server, nativeDevice, listener, explicit, priority);
	}

	public BleServer.ConnectionFailListener.Status getStatus()
	{
		return m_status;
	}

	@Override public void execute()
	{
		final BluetoothGattServer server_native_nullable = getServer().getNative();

		if( server_native_nullable == null )
		{
			if( !getServer().m_nativeWrapper.openServer() )
			{
				m_status = BleServer.ConnectionFailListener.Status.SERVER_OPENING_FAILED;

				failImmediately();

				return;
			}
		}

		final BluetoothGattServer server_native = getServer().getNative();

		if( server_native /*still*/ == null )
		{
			getManager().ASSERT(false, "Server should not be null after successfully opening!");

			m_status = BleServer.ConnectionFailListener.Status.SERVER_OPENING_FAILED;

			failImmediately();
		}
		else
		{
			if( getServer().m_nativeWrapper.isDisconnected(m_nativeDevice.getAddress()) )
			{
				if( server_native.connect(m_nativeDevice, false) )
				{
					// SUCCESS! At least, we will wait and see.
				}
				else
				{
					m_status = BleServer.ConnectionFailListener.Status.NATIVE_CONNECTION_FAILED;

					failImmediately();
				}
			}
			else
			{
				if( getServer().m_nativeWrapper.isDisconnecting(m_nativeDevice.getAddress()) )
				{
					getManager().ASSERT(false, "Server is currently disconnecting a client when we're trying to connect.");

					m_status = BleServer.ConnectionFailListener.Status.NATIVE_CONNECTION_FAILED;

					failImmediately();
				}
				else if( getServer().m_nativeWrapper.isConnecting(m_nativeDevice.getAddress()) )
				{
					//--- DRK > We don't fail out, but this is a good sign that something's amiss upstream.
					getManager().ASSERT(false, "Server is already connecting to the given client.");
				}
				else if( getServer().m_nativeWrapper.isConnected(m_nativeDevice.getAddress()) )
				{
					m_status = BleServer.ConnectionFailListener.Status.NATIVE_CONNECTION_FAILED;

					redundant();
				}
				else
				{
					getManager().ASSERT(false, "Native server state didn't match any expected values.");

					failImmediately();
				}
			}
		}
	}

	@Override public boolean isCancellableBy(PA_Task task)
	{
		if( task instanceof P_Task_DisconnectServer )
		{
			if( task.getClass() == P_Task_DisconnectServer.class && this.getServer().equals(task.getServer()) )
			{
				final P_Task_DisconnectServer task_cast = (P_Task_DisconnectServer) task;

				if( task_cast.m_nativeDevice.getAddress().equals(m_nativeDevice.getAddress()) )
				{
					//--- DRK > If an implicit disconnect comes in we have no choice but to bail.
					//---		Otherwise we let the connection task run its course then we'll
					//---		disconnect afterwards all nice and orderly-like.
					if( !task_cast.isExplicit() )
					{
						return true;
					}
				}
			}
		}
		else if( task instanceof P_Task_TurnBleOff )
		{
			return true;
		}

		return super.isCancellableBy(task);
	}

	@Override protected boolean isSoftlyCancellableBy(PA_Task task)
	{
		if( task.getClass() == P_Task_DisconnectServer.class && this.getServer().equals(task.getServer()) )
		{
			final P_Task_DisconnectServer task_cast = (P_Task_DisconnectServer) task;

			if( task_cast.m_nativeDevice.getAddress().equals(m_nativeDevice.getAddress()) )
			{
				if( this.isExplicit() )
				{
					return true;
				}
			}
		}

		return super.isSoftlyCancellableBy(task);
	}

	public void onNativeFail(final int gattStatus)
	{
		m_gattStatus = gattStatus;

		fail();
	}

	@Override protected BleTask getTaskType()
	{
		return BleTask.CONNECT_SERVER;
	}
}
