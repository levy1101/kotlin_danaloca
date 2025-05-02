# Danaloca - Ứng dụng Mạng xã hội

## Giới thiệu

Danaloca là một ứng dụng mạng xã hội di động hiện đại được phát triển trên nền tảng Android, cho phép người dùng kết nối, chia sẻ và tương tác với nhau một cách dễ dàng.

## Công nghệ sử dụng

### 1. Nền tảng và Ngôn ngữ

- **Android**: Nền tảng phát triển chính
- **Kotlin**: Ngôn ngữ lập trình chính
- **Gradle**: Hệ thống build tự động

### 2. Kiến trúc và Design Pattern

- **MVVM (Model-View-ViewModel)**: Mô hình kiến trúc chính
  - Model: Các lớp data class như User, Post, ChatMessage
  - View: Activities và Fragments
  - ViewModel: Các lớp quản lý logic như UserViewModel, PostViewModel
- **Repository Pattern**: Tách biệt logic truy cập dữ liệu
- **Single Activity Pattern**: Sử dụng Navigation Component

### 3. Firebase Services

- **Firebase Authentication**: Xác thực người dùng
- **Firebase Realtime Database**: Lưu trữ dữ liệu
- **Firebase Cloud Messaging (FCM)**: Gửi thông báo

### 4. Thư viện và Dependencies

- **Navigation Component**: Điều hướng trong ứng dụng
- **Glide**: Tải và hiển thị hình ảnh
- **Mapbox**: Tích hợp bản đồ và vị trí
- **Lottie**: Hiển thị animation JSON
- **Cloudinary**: Lưu trữ và quản lý hình ảnh
- **UCrop**: Chỉnh sửa và cắt ảnh
- **CircleImageView**: Hiển thị ảnh dạng tròn
- **Shimmer**: Hiệu ứng loading cho giao diện

## Luồng ứng dụng và Tính năng

### 1. Xác thực và Quản lý Phiên đăng nhập

- **Splash Screen**: Kiểm tra trạng thái đăng nhập khi khởi động
- **Đăng ký tài khoản**:
  - Xác thực email/mật khẩu
  - Tạo hồ sơ người dùng trong Firebase Realtime Database
  - Xử lý lỗi và thông báo người dùng
- **Đăng nhập**:
  - Sử dụng Firebase Auth
  - Animation chào mừng kiểu typewriter
  - Hiệu ứng fade-in cho form đăng nhập
  - Validate thông tin đầu vào
- **Quản lý phiên**:
  - Tự động đăng nhập cho phiên hiện tại
  - Xử lý đăng xuất và xóa dữ liệu phiên

### 2. Quản lý Bài viết

- **Tạo Bài Viết**:
  - Giao diện tạo bài viết với content và media
  - Quản lý quyền truy cập storage
  - Image picker tích hợp
  - Xử lý ảnh với UCrop:
    - Cắt ảnh tỷ lệ 1:1
    - Giới hạn kích thước 1080x1080
    - Nén ảnh với chất lượng 85%
    - Giao diện crop tùy chỉnh
- **Xử lý Hình Ảnh**:
  - Upload ảnh lên Cloudinary
  - Tối ưu hóa dung lượng và chất lượng
  - Cache local cho hiệu suất
  - Preview ảnh real-time
- **Dữ liệu Bài Viết**:
  - Lưu trữ trên Firebase Realtime DB
  - Thông tin: nội dung, URL ảnh, vị trí, timestamps
  - Tracking người like với Map structure
  - Sắp xếp theo thời gian đăng
- **Tương tác**:
  - Hệ thống like/unlike
  - Theo dõi trạng thái like của từng user
  - Cập nhật real-time số lượt like
  - Xóa bài viết với cascade delete

### 3. Kết bạn và Tương tác

- Tìm kiếm và xem danh sách người dùng
- Hệ thống kết bạn đầy đủ:
  - Gửi/hủy lời mời kết bạn
  - Chấp nhận/từ chối lời mời
  - Theo dõi trạng thái lời mời (Đang chờ, Đã chấp nhận, Đã từ chối, Đã hủy)
- Chat trực tiếp với bạn bè

### 4. Hệ thống Chat

- **Kiến trúc Chat**:
  - Sử dụng Firebase Realtime Database
  - Model ChatMessage với các trường:
    - ID tin nhắn duy nhất
    - ID người gửi và người nhận
    - Nội dung tin nhắn
    - Timestamp cho sắp xếp và hiển thị
- **Tính năng Real-time**:
  - Lắng nghe sự thay đổi với ValueEventListener
  - Cập nhật UI tức thì khi có tin nhắn mới
  - Quản lý subscription để tránh memory leak
  - Error handling cho network issues
- **Xử lý Dữ liệu Chat**:
  - Lọc tin nhắn theo người gửi/nhận
  - Sắp xếp theo timestamp
  - Phân trang và load theo batch
  - Tối ưu hiệu suất truy vấn
- **UI/UX Chat**:
  - Giao diện chat dạng bubble
  - Hiển thị thời gian tin nhắn
  - Input field với validation
  - Loading state và error handling

### 5. Tích hợp Bản đồ và Vị trí

- **Tích hợp Mapbox**:
  - Hiển thị bản đồ tương tác với Mapbox Streets style
  - Quản lý camera và mức độ zoom
  - Xử lý sự kiện click trên bản đồ
  - Annotation system cho markers
- **Chọn Vị trí**:
  - Giao diện riêng cho chọn vị trí (MapLocationActivity)
  - Tùy chỉnh vị trí marker với click
  - Điều khiển zoom in/out
  - Khởi tạo mặc định tại Đà Nẵng (108.2022, 16.0544)
- **Quản lý Markers**:
  - Thêm/xóa markers động
  - Custom marker icons
  - Chỉ cho phép một marker tại một thời điểm
  - Xác nhận vị trí với latitude/longitude
- **UI/UX cho Maps**:
  - Nút điều hướng: back, zoom, clear location
  - Hiển thị/ẩn controls dựa trên trạng thái
  - Tích hợp với flow tạo bài viết

### 6. Thiết kế UI/UX

- **Theme và Styles**:
  - Material Design với theme tùy chỉnh
  - Bảng màu nhất quán: primary, secondary, surface colors
  - Typography với font Poppins và Quicksand
  - Status bar và background colors được tối ưu
- **Navigation**:
  - Bottom Navigation với Curved design
  - Animated Vector Drawables cho icons
  - Fragment-based navigation với NavController
  - Custom transitions giữa các màn hình
- **Animations và Effects**:
  - Typewriter effect cho welcome text
  - Fade-in animations cho forms
  - Loading indicators với Shimmer effect
  - Smooth transitions cho navigation
- **Custom Views**:
  - CircleImageView cho avatar
  - Custom input fields với Material Design
  - Curved bottom navigation bar
  - Location preview dialog
- **Responsive Design**:
  - Layout tự động điều chỉnh với soft keyboard
  - Portrait mode optimization
  - Proper content scaling

### 7. Thông báo và Tính năng Phụ trợ

- **Coze Chat Integration**:
  - WebView tích hợp với JavaScript enabled
  - Custom user agent và security settings
  - Debug mode cho development
  - Cache management
- **Image Management**:
  - Glide cho load và cache hình ảnh
  - Tối ưu bộ nhớ và hiệu suất
  - Placeholder và error handling
  - Cache strategy thông minh

## Các thành phần chính

### Activities

- MainActivity: Activity chính
- HomeActivity: Màn hình chính
- LoginActivity: Đăng nhập
- RegisterActivity: Đăng ký
- CreatePostActivity: Tạo bài viết
- MapLocationActivity: Chọn vị trí
- SearchActivity: Tìm kiếm

### Fragments

- HomeFragment: Hiển thị feed
- ChatFragment: Tin nhắn
- FriendsFragment: Quản lý bạn bè
- NotificationsFragment: Thông báo
- ProfileFragment: Thông tin cá nhân
- SettingsFragment: Cài đặt

### Services

- MyFirebaseMessagingService: Xử lý thông báo

### Utils

- DrawableUtils: Xử lý hình ảnh
- GlideUtils: Quản lý tải hình ảnh
- ImageUtils: Xử lý ảnh
- Resource: Quản lý tài nguyên

## Yêu cầu hệ thống

- Android 5.0 (API level 21) trở lên
- Google Play Services
- Kết nối internet
- Quyền truy cập vị trí và storage

## Hướng phát triển tương lai

- Tích hợp video call
- Thêm tính năng story
- Hỗ trợ đa ngôn ngữ
- Tối ưu hiệu suất
- Thêm tính năng streaming
