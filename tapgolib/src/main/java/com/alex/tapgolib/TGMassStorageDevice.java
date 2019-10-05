package com.alex.tapgolib;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.partition.Partition;
import com.github.mjdev.libaums.partition.PartitionTypes;

import java.util.ArrayList;

/**
 * Created by AlexHe on 2019-10-01.
 * Describe
 */

public class TGMassStorageDevice {
    private final int EXFAT_TYPE_IN_ABSTRACTFILESYSTEM = 96919910;
    private ArrayList partitionsList = new ArrayList();
    private UsbMassStorageDevice usb;
    public TGMassStorageDevice(UsbMassStorageDevice usb) {
        this.usb = usb;
    }
    public ArrayList getPartitions() {
        if (usb.getInited()){
            for (Partition partition : usb.partitions) {
                FileSystem partitionFS = partition.getFileSystem();
                ArrayList partitionsInfo = new ArrayList();
                partitionsInfo.add(partitionFS.getVolumeLabel());
                if (partitionFS.getType() == EXFAT_TYPE_IN_ABSTRACTFILESYSTEM) {
                    partitionsInfo.add(PartitionTypes.NTFS_EXFAT);
                }
                else {
                    partitionsInfo.add(partitionFS.getType());
                }
                partitionsInfo.add(partitionFS.getCapacity());
                partitionsInfo.add(partitionFS.getOccupiedSpace());
                partitionsInfo.add(partitionFS.getFreeSpace());
                partitionsInfo.add(partitionFS.getChunkSize());

                partitionsList.add(partitionsInfo);
            }
            return partitionsList;
        }
        else {
            throw new UnsupportedOperationException("Not UsbMassStorageDevice.init() yet.");
        }
    }
}
