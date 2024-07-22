/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Collections.Generic;

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// Class used to hold BitArray objects for use in Message Audits.
    /// </summary>
    public class BitArrayBin
    {
        private List<BitArray> list;
        private int maxNumberOfArrays;
        private int firstIndex = -1;
        private long lastInOrderBit = -1;

        public BitArrayBin(int windowSize)
        {
            maxNumberOfArrays = ((windowSize + 1) / BitArray.LONG_SIZE) + 1;
            maxNumberOfArrays = Math.Max(maxNumberOfArrays, 1);
            list = new List<BitArray>();
            for (int i = 0; i < maxNumberOfArrays; i++)
            {
                list.Add((BitArray)null);
            }
        }

        public bool SetBit(long index, bool val)
        {
            bool answer = false;
            BitArray ba = GetBitArray(index);
            if (ba != null)
            {
                int offset = GetOffset(index);
                if (offset >= 0)
                {
                    answer = ba.Get(offset);
                    ba.Set(offset, val);
                }
            }
            return answer;
        }

        /// <summary>
        /// Test if the next message is in order.
        /// </summary>
        public bool IsInOrder(long index)
        {
            bool result = false;
            if (lastInOrderBit == -1)
            {
                result = true;
            }
            else
            {
                result = lastInOrderBit + 1 == index;
            }
            lastInOrderBit = index;
            return result;
    
        }
    
        /// <summary>
        /// Get the boolean value at the index
        /// </summary>
        public bool GetBit(long index)
        {
            bool answer = index >= firstIndex;
            BitArray ba = GetBitArray(index);
            if (ba != null)
            {
                int offset = GetOffset(index);
                if (offset >= 0)
                {
                    answer = ba.Get(offset);
                    return answer;
                }
            }
            else
            {
                // gone passed range for previous bins so assume set
                answer = true;
            }
            return answer;
        }
    
        /// <summary>
        /// Get the BitArray for the index
        /// </summary>
        private BitArray GetBitArray(long index)
        {
            int bin = GetBin(index);
            BitArray answer = null;
            if (bin >= 0)
            {
                if (bin >= maxNumberOfArrays)
                {
                    int overShoot = bin - maxNumberOfArrays + 1;
                    while (overShoot > 0)
                    {
                        list.RemoveAt(0);
                        firstIndex += BitArray.LONG_SIZE;
                        list.Add(new BitArray());
                        overShoot--;
                    }

                    bin = maxNumberOfArrays - 1;
                }
                answer = list[bin];
                if (answer == null)
                {
                    answer = new BitArray();
                    list[bin] = answer;
                }
            }
            return answer;
        }

        /// <summary>
        /// Get the index of the bin from the total index
        /// </summary>
        private int GetBin(long index)
        {
            int answer = 0;
            if (firstIndex < 0)
            {
                firstIndex = (int) (index - (index % BitArray.LONG_SIZE));
            }
            else if (firstIndex >= 0)
            {
                answer = (int)((index - firstIndex) / BitArray.LONG_SIZE);
            }
            return answer;
        }
    
        /// <summary>
        /// Get the offset into a bin from the total index
        /// </summary>
        private int GetOffset(long index)
        {
            int answer = 0;
            if (firstIndex >= 0)
            {
                answer = (int)((index - firstIndex) - (BitArray.LONG_SIZE * GetBin(index)));
            }
            return answer;
        }
    
        public long GetLastSetIndex()
        {
            long result = -1;
            
            if (firstIndex >= 0)
            {
                result = firstIndex;   
                BitArray last = null;
                for (int lastBitArrayIndex = maxNumberOfArrays - 1; lastBitArrayIndex >= 0; lastBitArrayIndex--)
                {
                    last = list[lastBitArrayIndex];
                    if (last != null)
                    {
                        result += last.Length - 1;
                        result += lastBitArrayIndex * BitArray.LONG_SIZE;
                        break;
                    }
                }
            }
            return result;
        }
    }
}

