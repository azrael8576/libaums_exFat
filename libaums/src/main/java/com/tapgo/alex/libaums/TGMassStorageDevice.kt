package com.tapgo.alex.libaums

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.partition.PartitionTypes


/**
 * Class representing a connected USB mass storage device. You can enumerate
 * through all connected mass storage devices via
 * [.getMassStorageDevices]. This method only returns supported
 * devices or if no device is connected an empty array.
 *
 *
 * After choosing a device you have to get the permission for the underlying
 * [android.hardware.usb.UsbDevice]. The underlying
 * [android.hardware.usb.UsbDevice] can be accessed via
 * [.getUsbDevice].
 *
 *
 * After that you need to call [.setupDevice]. This will initialize the
 * mass storage device and read the partitions (
 * [com.github.mjdev.libaums.partition.Partition]).
 *
 *
 * The supported partitions can then be accessed via [.getPartitions]
 * and you can begin to read directories and files.
 *
 * @author mjahnen
 */
class TGMassStorageDevice(private val usb: UsbMassStorageDevice) {
    private val EXFAT_TYPE_IN_ABSTRACTFILESYSTEM = 96919910
    private var partitionsList = arrayListOf<Any>()
    fun getPartitions(): ArrayList<Any> {
        if (usb.inited) {
            for (partition in usb.partitions) {
                var partitionFS = partition.fileSystem
                var partitionsInfo = arrayListOf<Any>()
                partitionsInfo.add(partitionFS.volumeLabel)
                if (partitionFS.type == EXFAT_TYPE_IN_ABSTRACTFILESYSTEM) {
                    partitionsInfo.add(PartitionTypes.NTFS_EXFAT)
                }
                else {
                    partitionsInfo.add(partitionFS.type)
                }
                partitionsInfo.add(partitionFS.capacity)
                partitionsInfo.add(partitionFS.occupiedSpace)
                partitionsInfo.add(partitionFS.freeSpace)
                partitionsInfo.add(partitionFS.chunkSize)
                partitionsInfo.add(partitionFS.rootDirectory)

                partitionsList.add(partitionsInfo)
            }
            return partitionsList
        }
        else {
            throw UnsupportedOperationException("Not UsbMassStorageDevice.init() yet.")
        }
    }
}
