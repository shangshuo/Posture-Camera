# 体态相机 (Posture Camera)

通过摄像头和陀螺仪传感器检测并记录用户体态信息的 Android 应用。

## 功能

- **实时体态检测**：使用 CameraX 实时预览，结合陀螺仪传感器检测头部姿态
- **姿态辅助线**：在相机预览画面中绘制十字参考线，帮助用户对齐姿态
- **数据记录**：记录体态检测数据，支持本地存储和查看历史趋势
- **水面印处理**：支持在照片上叠加水印

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material 3
- **相机**：CameraX
- **传感器**：SensorManager (陀螺仪)
- **本地存储**：Room
- **架构**：MVVM (ViewModel + StateFlow)

## 系统要求

- Android 7.0 (API 24) 及以上
- 需要摄像头和陀螺仪传感器

## 下载

本应用已在 **华为应用市场** 上架，直接搜索 **"体态相机"** 即可下载安装。

也可在 [GitHub Releases](https://github.com/shangshuo/Posture-Camera/releases) 下载 APK 直接安装。

## 构建

```bash
./gradlew assembleDebug
```

## 许可证

MIT License

```
MIT License

Copyright (c) 2026 Shuo Shang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```