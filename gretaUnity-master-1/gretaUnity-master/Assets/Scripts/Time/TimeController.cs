using System;

namespace time
{
    public class TimeController
    {
        private long offset;
        private bool synchronized;

        public TimeController()
        {
            /**
            * The offset between the current time and the system one. (in milliseconds)
            */
            offset = DateTime.Now.Ticks / 10000;
            synchronized = false;
        }

        public long getTimeMillis()
        {
            return DateTime.Now.Ticks / 10000 - offset;
        }

        public void setTimeMillis(long milliSeconds)
        {
            offset = DateTime.Now.Ticks / 10000 - milliSeconds;//+400;// the +400 is to take into account delays on the network
            synchronized = true;
        }

        public double getTime()
        {
            return (double)getTimeMillis() / 1000.0;
        }

        public void setTime(double seconds)
        {
            setTimeMillis((long)(seconds * 1000));
        }

        public bool isSynchronized()
        {
            return synchronized;
        }
        public void setNotSynchronized()
        {
            synchronized = false;
        }
    }
}
