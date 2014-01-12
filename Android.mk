LOCAL_PATH:= $(call my-dir)
include $(call all-subdir-makefiles)
########################################################
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := listviewanimations_prebuilt

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := listviewanimations_prebuilt:com.haarman.listviewanimations-2.5.2.jar

include $(BUILD_MULTI_PREBUILT)

########################################################
