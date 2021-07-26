using System;

namespace thrift.services
{
	public interface ConnectionListener
	{
		void onDisconnection();

    	void onConnection();
	}
}

