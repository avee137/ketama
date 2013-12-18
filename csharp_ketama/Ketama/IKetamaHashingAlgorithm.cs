using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ketama
{
    /// <summary>
    /// Interface for the Ketama hashing algorithm, to allow extensibility if we eventually implement serveral 
    /// algorithms.
    /// </summary>
    public interface IKetamaHashingAlgorithm
    {
        uint GetHashFromString(string key, uint offset);
    }
}
