LOCAL_PATH := $(call my-dir)

#####################################################################

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := nineoldandroids stickylistheaders

LOCAL_SRC_FILES := $(call all-subdir-java-files, src)

LOCAL_MODULE := ListViewAnimations

include $(BUILD_STATIC_JAVA_LIBRARY)

#####################################################################

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := nineoldandroids:libs/nineoldandroids-2.4.0.jar

include $(BUILD_MULTI_PREBUILT)

#####################################################################

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := stickylistheaders:libs/stickylistheaders_lib.jar

include $(BUILD_MULTI_PREBUILT)
