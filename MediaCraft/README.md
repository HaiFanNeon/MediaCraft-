
MediaCraft 开发实战指南 🛠️
这是一个基于 Kotlin + MVVM + Hilt + FFmpeg 的安卓视频压缩 App 完整开发流程。

阶段一：基础设施搭建 (Infrastructure)
目标：搭建地基，确保 App 能跑起来，不闪退。

1.1 项目配置 (Gradle)
[√] 在 libs.versions.toml 或 build.gradle 中添加依赖：
Hilt: 用于依赖注入。
FFmpeg-Kit: 用于视频处理 (com.arthenica:ffmpeg-kit-full:6.0-2)。
Glide: 用于加载视频封面图。
Coroutines: 用于异步操作。
ViewBinding: 在 android { ... } 中开启。
1.2 Hilt 初始化 (最重要的地基)
[√] 创建 MediaCraftApp.kt：
继承 Application 类。
添加 @HiltAndroidApp 注解。
[√] 修改 AndroidManifest.xml：
在 <application> 标签中添加 android:name=".MediaCraftApp"。
[√] 配置 MainActivity.kt：
添加 @AndroidEntryPoint 注解。
阶段二：数据层开发 (Data Layer)
目标：编写能从手机存储中“挖”出视频数据的代码。

2.1 定义数据模型 (Model)
[ ] 创建 data/model/VideoItem.kt：
这是一个 data class。
属性包括：id (Long), uri (Uri), name (String), duration (Long), size (Long), path (String)。
作用：这是我们在 App 里传递视频信息的标准格式。
2.2 编写仓库 (Repository)
[ ] 创建 data/repository/VideoRepository.kt：
使用 @Inject 注入 Context。
编写 suspend fun getAllVideos(): List<VideoItem> 方法。
核心逻辑：
使用 contentResolver.query 查询 MediaStore.Video.Media.EXTERNAL_CONTENT_URI。
设置 projection（我们要查哪些列：文件名、路径、时长）。
设置 selection（筛选条件：只查 MP4，或者查所有视频）。
循环游标 (Cursor)，把查到的数据转换成 VideoItem 列表并返回。
阶段三：业务逻辑层 (ViewModel)
目标：连接 UI 和 数据，处理“加载中”、“加载成功”、“加载失败”的状态。

3.1 定义 UI 状态
[ ] 创建 ui/state/VideoUiState.kt (可选，也可以写在 ViewModel 内部)：
sealed class VideoUiState
object Loading: 代表正在扫描。
data class Success(val videos: List<VideoItem>): 扫描到了数据。
data class Error(val message: String): 没权限或出错了。
3.2 编写 ViewModel
[ ] 创建 ui/MainViewModel.kt：
添加 @HiltViewModel 注解。
构造函数注入 VideoRepository。
创建 _uiState (MutableStateFlow) 和 uiState (StateFlow) 供界面观察。
编写 loadVideos() 方法：
开启 viewModelScope.launch 协程。
发送 Loading 状态。
调用仓库的 getAllVideos()。
发送 Success 状态。
用 try-catch 捕获异常，如果出错发送 Error 状态。
阶段四：界面层开发 (UI Implementation)
目标：画出界面，申请权限，展示列表。

4.1 布局文件 (Layouts)
[ ] 创建列表项布局 res/layout/item_video.xml：
CardView: 卡片容器。
ImageView: 显示视频缩略图 (id: ivThumbnail)。
TextView: 显示文件名 (id: tvName)。
TextView: 显示时长/大小 (id: tvInfo)。
[ ] 修改主布局 res/layout/activity_main.xml：
添加 RecyclerView (id: recyclerView)。
添加 ProgressBar (id: progressBar)，默认设为隐藏，加载时显示。
4.2 列表适配器 (Adapter)
[ ] 创建 ui/adapter/VideoAdapter.kt：
继承 ListAdapter<VideoItem, VideoAdapter.VideoViewHolder>。
实现 DiffUtil.ItemCallback (用于高效刷新列表)。
在 onBindViewHolder 中：
使用 Glide 加载 item.uri 到 imageView。
设置文字信息。
设置点击事件：root.setOnClickListener { onVideoClick(item) }。
4.3 权限处理与数据绑定 (MainActivity)
[ ] 在 MainActivity.kt 中：
声明 private val viewModel: MainViewModel by viewModels()。
权限申请逻辑：
使用 registerForActivityResult 注册权限回调。
判断版本：
Android 13+ (SDK 33): 申请 READ_MEDIA_VIDEO。
Android 12及以下: 申请 READ_EXTERNAL_STORAGE。
UI 初始化：
设置 RecyclerView 的 LayoutManager 和 Adapter。
数据观察：
lifecycleScope.launch { viewModel.uiState.collect { state -> ... } }
根据 state (Loading/Success) 切换 ProgressBar 和 RecyclerView 的显示/隐藏。
阶段五：FFmpeg 核心功能 (The Engine)
目标：实现视频压缩。

5.1 压缩逻辑
[ ] 在 MainViewModel 或新建 CompressionManager 中：
编写构建 FFmpeg 命令的方法。例如： "-i ${inputPath} -vcodec libx264 -crf 28 -preset ultrafast ${outputPath}"
解释：CRF 28 是压缩质量平衡点，数值越大文件越小画质越差。
5.2 执行压缩
[ ] 处理点击事件：
当用户点击列表中的视频，弹出一个 Dialog 确认。
生成输出路径 (例如 Movies/MediaCraft/output.mp4)。
[ ] 调用 FFmpegKit：
kotlin
FFmpegKit.executeAsync(command) { session ->
val returnCode = session.returnCode
if (ReturnCode.isSuccess(returnCode)) {
// 成功：通知 UI，刷新相册
} else {
// 失败：打印日志
}
}
5.3 进度反馈 (进阶)
[ ] 监听进度：
使用 FFmpegKitConfig.enableStatisticsCallback。
计算百分比：(当前时间 / 视频总时长) * 100。
更新 UI 上的进度条。