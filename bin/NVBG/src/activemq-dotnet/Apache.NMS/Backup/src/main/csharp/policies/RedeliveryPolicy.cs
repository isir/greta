/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;

namespace Apache.NMS.Policies
{
    /// <summary>
    /// A policy used to customize exactly how you want the redelivery to work.
    /// </summary>
    public class RedeliveryPolicy : IRedeliveryPolicy
    {
        private static readonly object syncObject = new object();

        private double collisionAvoidanceFactor = .15;
        private int initialRedeliveryDelay = 1000;
        private int maximumRedeliveries = 6;
        private int backOffMultiplier = 5;
        private bool useCollisionAvoidance = false;
        private bool useExponentialBackOff = false;

        private static Random randomNumberGenerator;
        private static bool nextBool = false;

        #region IRedeliveryPolicy Members

        public int CollisionAvoidancePercent
        {
            get { return Convert.ToInt32(Math.Round(collisionAvoidanceFactor * 100)); }
            set { collisionAvoidanceFactor = Convert.ToDouble(value) * .01; }
        }

        public bool UseCollisionAvoidance
        {
            get { return this.useCollisionAvoidance; }
            set { this.useCollisionAvoidance = value; }
        }

        public int InitialRedeliveryDelay
        {
            get { return this.initialRedeliveryDelay; }
            set { this.initialRedeliveryDelay = value; }
        }

        public int MaximumRedeliveries
        {
            get { return this.maximumRedeliveries; }
            set { this.maximumRedeliveries = value; }
        }

        public virtual int RedeliveryDelay(int redeliveredCounter)
        {
            int delay = 0;

            if(redeliveredCounter == 0)
            {
                // The first time through there is no delay, the Rollback should be immediate.
                return 0;
            }

            if(UseExponentialBackOff && BackOffMultiplier > 1)
            {
                delay = initialRedeliveryDelay * Convert.ToInt32(Math.Pow(BackOffMultiplier, redeliveredCounter - 1));
            }
            else
            {
                delay = InitialRedeliveryDelay;
            }

            if(UseCollisionAvoidance)
            {
                Random random = RandomNumberGenerator;
                double variance = (NextBool ? collisionAvoidanceFactor : collisionAvoidanceFactor *= -1) * random.NextDouble();
                delay += Convert.ToInt32(Convert.ToDouble(delay) * variance);
            }

            return delay;
        }

        public bool UseExponentialBackOff
        {
            get { return this.useExponentialBackOff; }
            set { this.useExponentialBackOff = value; }
        }

        public int BackOffMultiplier
        {
            get { return backOffMultiplier; }
            set { backOffMultiplier = value; }
        }

        #endregion

        /// <summary>
        /// Gets the random number generator.
        /// </summary>
        /// <value>The random number generator.</value>
        protected static Random RandomNumberGenerator
        {
            get
            {
                if(randomNumberGenerator == null)
                {
                    lock(syncObject)
                    {
                        if(randomNumberGenerator == null)
                        {
                            randomNumberGenerator = new Random(DateTime.Now.Second);
                        }
                    }
                }

                return randomNumberGenerator;
            }
        }

        /// <summary>
        /// Gets the next boolean
        /// </summary>
        /// <value><c>true</c> if [next bool]; otherwise, <c>false</c>.</value>
        protected static bool NextBool
        {
            get
            {
                lock(syncObject)
                {
                    nextBool = !nextBool;
                    return nextBool;
                }
            }
        }

        /// <summery>
        /// Clone this object and return a new instance that the caller now owns.
        /// </summery>
        public Object Clone()
        {
            // Since we are a derived class use the base's Clone()
            // to perform the shallow copy. Since it is shallow it
            // will include our derived class. Since we are derived,
            // this method is an override.
            return this.MemberwiseClone();
        }

    }
}
