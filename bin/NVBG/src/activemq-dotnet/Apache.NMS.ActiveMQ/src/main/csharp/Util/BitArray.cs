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

namespace Apache.NMS.ActiveMQ.Util
{
    /// <summary>
    /// A specialized BitArray implementation that provides the smallest set
    /// of functionality needed for Message Auditing.  This implementation is
    /// used over the .NET bit array to provide a small and more efficient
    /// BitArray that performs only the operations needed for Message Audit.
    /// </summary>
    public class BitArray
    {
        public const int LONG_SIZE = 64;
        public const int INT_SIZE = 32;
        public const int SHORT_SIZE = 16;
        public const int BYTE_SIZE = 8;
        private static readonly ulong[] BIT_VALUES = {0x0000000000000001UL, 0x0000000000000002UL, 0x0000000000000004UL,
                                                      0x0000000000000008UL, 0x0000000000000010UL, 0x0000000000000020UL,
                                                      0x0000000000000040UL, 0x0000000000000080UL, 0x0000000000000100UL,
                                                      0x0000000000000200UL, 0x0000000000000400UL, 0x0000000000000800UL,
                                                      0x0000000000001000UL, 0x0000000000002000UL, 0x0000000000004000UL,
                                                      0x0000000000008000UL, 0x0000000000010000UL, 0x0000000000020000UL,
                                                      0x0000000000040000UL, 0x0000000000080000UL, 0x0000000000100000UL,
                                                      0x0000000000200000UL, 0x0000000000400000UL, 0x0000000000800000UL,
                                                      0x0000000001000000UL, 0x0000000002000000UL, 0x0000000004000000UL,
                                                      0x0000000008000000UL, 0x0000000010000000UL, 0x0000000020000000UL,
                                                      0x0000000040000000UL, 0x0000000080000000UL, 0x0000000100000000UL,
                                                      0x0000000200000000UL, 0x0000000400000000UL, 0x0000000800000000UL,
                                                      0x0000001000000000UL, 0x0000002000000000UL, 0x0000004000000000UL,
                                                      0x0000008000000000UL, 0x0000010000000000UL, 0x0000020000000000UL,
                                                      0x0000040000000000UL, 0x0000080000000000UL, 0x0000100000000000UL,
                                                      0x0000200000000000UL, 0x0000400000000000UL, 0x0000800000000000UL,
                                                      0x0001000000000000UL, 0x0002000000000000UL, 0x0004000000000000UL,
                                                      0x0008000000000000UL, 0x0010000000000000UL, 0x0020000000000000UL,
                                                      0x0040000000000000UL, 0x0080000000000000UL, 0x0100000000000000UL,
                                                      0x0200000000000000UL, 0x0400000000000000UL, 0x0800000000000000UL,
                                                      0x1000000000000000UL, 0x2000000000000000UL, 0x4000000000000000UL,
                                                      0x8000000000000000UL};
        private ulong bits = 0;
        private int length = 0;

        public BitArray()
        {
        }

        public BitArray(long bits)
        {
            this.bits = (ulong)bits;
        }

        /// <summary>
        /// Returns the current length of the bits that have been
        /// set so far in this BitArray.
        /// </summary>
        public int Length
        {
           get { return length; }
        }
    
        /// <summary>
        /// Returns the actual long value containing all the set bits.
        /// </summary>
        public long Bits
        {
            get { return (long)bits; }
        }
    
        /// <summary>
        /// Sets the boolean value of the given bit in the array at the specified index.
        /// </summary>
        public bool Set(int index, bool flag)
        {
            length = Math.Max(length, index + 1);
            bool oldValue = (bits & BIT_VALUES[index]) != 0;
            if (flag)
            {
                bits |= BIT_VALUES[index];
            }
            else if (oldValue)
            {
                bits &= ~(BIT_VALUES[index]);
            }
            return oldValue;
        }
    
        /// <summary>
        /// Get the boolean value contains in the BitArray at the given index
        /// </summary>
        public bool Get(int index)
        {
            return (bits & BIT_VALUES[index]) != 0;
        }
    
        /// <summary>
        /// Reset all the bits to zero or false.
        /// </summary>
        public void Reset()
        {
            bits = 0;
        }

        /// <summary>
        /// Reset all the bits to the given value
        /// </summary>
        public void Reset(long bits)
        {
            this.bits = (ulong)bits;
        }

    }
}

