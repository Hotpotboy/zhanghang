/*
 *
 * Copyright (c) 2015, alipay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 	art_method_replace_5_0.cpp
 *
 * @author : sanping.li@alipay.com
 *
 */
#include <time.h>
#include <stdlib.h>
#include <stddef.h>
#include <assert.h>

#include <stdbool.h>
#include <fcntl.h>
#include <dlfcn.h>

#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <ctype.h>
#include <errno.h>
#include <utime.h>
#include <sys/types.h>
#include <sys/wait.h>

#include "art.h"
#include "art_5_0.h"
#include "../common.h"

void replace_5_0(JNIEnv* env, jobject src, jobject dest) {
	art::mirror::ArtMethod* smeth =
			(art::mirror::ArtMethod*) env->FromReflectedMethod(src);

	art::mirror::ArtMethod* dmeth =
			(art::mirror::ArtMethod*) env->FromReflectedMethod(dest);

	dmeth->declaring_class_->class_loader_ = smeth->declaring_class_->class_loader_; //for plugin classloader方法对应的类的对应的类加载器
	dmeth->declaring_class_->clinit_thread_id_ = smeth->declaring_class_->clinit_thread_id_;//用来检测递归调用的ID
//	dmeth->declaring_class_->status_ = smeth->declaring_class_->status_-1;//类初始化状态

	smeth->declaring_class_ = dmeth->declaring_class_;//定义方法的类
	smeth->access_flags_ = dmeth->access_flags_;//方法的访问标识
	smeth->frame_size_in_bytes_ = dmeth->frame_size_in_bytes_;//帧的总数量
	smeth->dex_cache_initialized_static_storage_ = dmeth->dex_cache_initialized_static_storage_;//缓存此方法包含的已经初始化了的静态存储区
	smeth->dex_cache_resolved_types_ = dmeth->dex_cache_resolved_types_;//缓存此方法包含的已经解析了的类型
	smeth->dex_cache_resolved_methods_ = dmeth->dex_cache_resolved_methods_;//缓存此方法包含的已经解析了的方法
	smeth->vmap_table_ = dmeth->vmap_table_;//虚拟寄存器与真实寄存器的映射表
	smeth->core_spill_mask_ = dmeth->core_spill_mask_;//依赖架构的注册器的溢出标记
	smeth->fp_spill_mask_ = dmeth->fp_spill_mask_;
	smeth->mapping_table_ = dmeth->mapping_table_;//本地程序计数器与dex程序计数器的映射表
	smeth->code_item_offset_ = dmeth->code_item_offset_;//Dex文件中代码Item的偏移量
	smeth->entry_point_from_compiled_code_ = dmeth->entry_point_from_compiled_code_;//本地指令执行时，本方法的入口点；可能是一个本地指令也可能是JNI方法执行的一个桥接方法

	smeth->entry_point_from_interpreter_ = dmeth->entry_point_from_interpreter_;//解释执行时，本方法的入口点
	smeth->native_method_ = dmeth->native_method_;//本地方法
	smeth->method_index_ = dmeth->method_index_;//方法索引
	smeth->method_dex_index_ = dmeth->method_dex_index_;//dex文件中方法标识列表中的索引

	LOGD("replace_5_0: %d , %d", smeth->entry_point_from_compiled_code_,
			dmeth->entry_point_from_compiled_code_);

}

void setFieldFlag_5_0(JNIEnv* env, jobject field) {
	art::mirror::ArtField* artField =
			(art::mirror::ArtField*) env->FromReflectedField(field);
	artField->access_flags_ = artField->access_flags_ & (~0x0002) | 0x0001;
	LOGD("setFieldFlag_5_0: %d ", artField->access_flags_);
}

