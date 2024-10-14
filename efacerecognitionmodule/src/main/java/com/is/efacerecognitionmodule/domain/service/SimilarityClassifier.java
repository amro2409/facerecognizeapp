/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.is.efacerecognitionmodule.domain.service;

import android.graphics.Bitmap;

import com.is.efacerecognitionmodule.data.model.Recognition;

import java.util.List;

/** Generic interface for interacting with different recognition engines. */
public interface SimilarityClassifier {

  /**
   * تسجيل التعرف الجديد في النظام.
   *
   * @param name اسم الشخص.
   * @param recognition الكائن الممثل للتعرف.
   */
  void register(String name, Recognition recognition);

  float [][]generateEmbedding(Bitmap bitmap);

  /**
   * التعرف على الصورة المعطاة.
   *
   * @param bitmap الصورة للمعالجة.
   * @param getExtra إذا كان هناك حاجة للحصول على معلومات إضافية.
   * @return قائمة بالتعرفات.
   */
  List<Recognition> recognizeImage(Bitmap bitmap, boolean getExtra);

  /**
   * تمكين تسجيل الإحصائيات لأغراض التصحيح.
   *
   * @param debug قيمة منطقية لتفعيل أو تعطيل التسجيل.
   */
  void enableStatLogging(final boolean debug);

  /**
   * الحصول على سلسلة الإحصائيات.
   *
   * @return سلسلة تحتوي على الإحصائيات.
   */
  String getStatString();

  void close();


  //void setNumThreads(int num_threads);


  /**
   * تمكين أو تعطيل استخدام واجهة برمجة التطبيقات الشبكية (NNAPI).
   *
   * @param isChecked قيمة منطقية لتفعيل أو تعطيل NNAPI.
   */
  void setUseNNAPI(boolean isChecked);


}
