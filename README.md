# Chợ Đồ Cũ (AppMuaBanDoCu) - Sàn Mua Bán Đồ Cũ Android Hiện Đại

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?logo=kotlin)](https://kotlinlang.org/)
[![JDK](https://img.shields.io/badge/JDK-Java%2021%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Gradle](https://img.shields.io/badge/Gradle-8.13-blue.svg?logo=gradle)](https://gradle.org/)
[![AGP](https://img.shields.io/badge/AGP-8.13.2-brightgreen.svg)](https://developer.android.com/build)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.02.00-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-indigo.svg)](https://m3.material.io/)
[![Design Rule](https://img.shields.io/badge/Color%20Palette-60--30--10%20Standard-brightgreen.svg)](#quy-chuẩn-thiết-kế--bảng-màu-60-30-10)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Auth%20%7C%20Firestore%20%7C%20Realtime%20DB-orange.svg?logo=firebase)](https://firebase.google.com/)
[![Cloudinary](https://img.shields.io/badge/Media-Cloudinary-lightblue.svg?logo=cloudinary)](https://cloudinary.com/)
[![License](https://img.shields.io/badge/License-MIT-brightgreen.svg)](LICENSE)

> **AppMuaBanDoCu** là ứng dụng di động thương mại điện tử chuyên biệt cho việc thanh lý, sang nhượng và mua bán đồ cũ (secondhand goods). Ứng dụng kết nối trực tiếp người mua và người bán, tối ưu hóa vòng đời đồ dùng, thúc đẩy tiêu dùng bền vững và bảo vệ môi trường với trải nghiệm giao diện chuẩn Material 3 cao cấp.

---

## Quy Chuẩn Thiết Kế & Bảng Màu (60-30-10 Rule)

Toàn bộ ứng dụng được chuẩn hóa giao diện theo **nguyên lý phối màu 60-30-10** trong thiết kế UI/UX hiện đại, đem lại cảm giác sang trọng, tinh tế và đáng tin cậy:

|  Tỷ lệ  | Ý nghĩa & Vai trò                | Mã màu                | Ứng dụng trong giao diện                                                                                                                      |
| :-----: | :------------------------------- | :-------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------- |
| **60%** | **Màu Chủ Đạo (Primary Canvas)** | `#FFFFFF` / `#F8FAFC` | Nền màn hình tinh khiết, bề mặt thẻ sản phẩm (Cards), danh sách lướt tin, tạo cảm giác sạch sẽ và thông thoáng.                               |
| **30%** | **Màu Thương Hiệu (Brand Navy)** | `#1E3A8A` / `#0D6EFD` | Thanh tiêu đề (TopAppBar), Bottom Navigation, tiêu đề mục, nút chính (Primary Buttons), icon định vị, biểu thị sự an tâm & uy tín giao dịch.  |
| **10%** | **Màu Nhấn (Accent Coral)**      | `#FF5722` / `#FF7043` | Giá bán sản phẩm (Price tags), nút Mua ngay / Trả giá, nút Đẩy tin, huy hiệu "Đã xác minh" / "Hot", cờ vi phạm, Floating Action Button (FAB). |

---

## 1. Hệ Thống Tính Năng Toàn Diện

### Dành Cho Người Dùng (End-User Features)

#### 1. Xác thực & Tài khoản

- **Đăng nhập & Đăng ký linh hoạt:** Hỗ trợ Email/Mật khẩu và Google Sign-In chỉ với 1 chạm.
- **Bảo mật phân quyền:** Tự động nhận diện vai trò người dùng thường (`MEMBER`) hoặc quản trị viên (`ADMIN`).
- **Bảo vệ tài khoản realtime:** Lắng nghe trạng thái khóa tài khoản trong thời gian thực. Nếu tài khoản bị Admin xử lý vi phạm, hệ thống tự động đăng xuất ngay lập tức và thông báo lý do rõ ràng.

#### 2. Trang chủ, Tìm kiếm nâng cao & Đồ cũ gần tôi (Near Me)

- **Realtime Search & Lịch sử tìm kiếm:**
  - Tìm kiếm tức thì theo từ khóa tên sản phẩm.
  - Lưu trữ lịch sử tìm kiếm gần đây dưới dạng thẻ Chips trực quan bên dưới thanh tìm kiếm (hỗ trợ nút **"Xóa lịch sử"**).
- **Đồ cũ gần tôi (Near Me):**
  - Chuyển đổi nhanh xem các mặt hàng đồ cũ được đăng bán tại khu vực lân cận (ví dụ: Hà Nội, TP.HCM, Đà Nẵng, Cần Thơ...).
  - Giúp người mua dễ dàng hẹn gặp xem đồ, kiểm tra trực tiếp tình trạng hàng trước khi thanh toán.
- **Danh mục đa dạng:** Thiết bị điện tử, Xe cộ, Thời trang đồ cũ, Đồ gia dụng, Khác...
- **Bộ lọc nâng cao & Sắp xếp bài viết (Advanced Filters & Sort):**
  - **Khoảng giá linh hoạt (Min - Max RangeSlider):** Kéo thanh trượt 2 đầu từ Min đến Max (từ 0 đ đến 20.000.000+ đ, ví dụ: 200k - 2 triệu).
  - **Lọc theo tình trạng hàng:** Lọc nhanh theo hiện trạng món đồ: "Mới 99%", "Đã qua sử dụng", "Hoạt động tốt".
  - **Khu vực mua sắm:** Lọc theo Tỉnh/Thành phố (chỉ xem đồ cũ tại khu vực của mình).
  - **Tùy chọn sắp xếp thông minh:** "Mới nhất", "Giá từ thấp đến cao", "Giá từ cao đến thấp", "Đánh giá cao nhất".

#### 3. Trung Tâm Thông Báo Thời Gian Thực (In-App Notification Center)

- **Màn hình thông báo chuyên nghiệp (`NotificationsScreen`):**
  - Tích hợp biểu tượng Chuông trên TopAppBar của Trang chủ với chấm đỏ báo tin chưa đọc.
  - Đồng bộ thời gian thực qua Firebase Realtime Database (`/notifications/{userId}`).
- **Đa dạng phân loại thông báo:**
  - **Tin nhắn mới:** Thông báo khi có người hỏi mua hoặc người bán phản hồi giá.
  - **Đánh giá & Nhận xét:** Thông báo khi có người để lại đánh giá uy tín giao dịch.
  - **Đẩy tin bài đăng:** Xác nhận bài viết đã được làm mới thành công lên đầu trang chủ.
  - **Ban Quản Trị:** Thông báo chính sách, khuyến cáo an toàn từ Admin.
  - **Cảnh báo vi phạm:** Cảnh báo tài khoản bị hạn chế hoặc lý do bị khóa.
- **Tiện ích:** Bộ lọc theo tab (_Tất cả, Tin nhắn & Giao dịch, Hệ thống_), Đánh dấu đã đọc tất cả, Xóa thông báo, Chạm vào để điều hướng tới trang liên quan.

#### 4. Đẩy Tin / Làm Mới Bài Đăng (Bump / Boost Post)

- **Cơ chế hoạt động:**
  - Trong trang **"Quản lý bài đăng"** (`ManageProductScreen`), mỗi bài viết có nút **"Đẩy tin"** (icon tên lửa).
  - Cho phép người bán làm mới bài đăng miễn phí mỗi **24 giờ một lần**.
  - Tự động cập nhật `timestamp` và `lastBumpedAt` lên Firebase Realtime Database, đưa bài viết lập tức nhảy lên vị trí đầu tiên của Trang chủ `HomeScreen`.
  - Có đồng hồ đếm ngược thông minh hiển thị thời gian còn lại (ví dụ: _"Đã đẩy (còn 18h)"_) kèm thông báo hướng dẫn khi chưa đủ thời gian.

#### 5. Nhắn Tin Trực Tiếp Người Mua & Bán (Realtime Chat)

- **Chat thời gian thực:** Đồng bộ tin nhắn qua Firebase Realtime Database.
- **Gắn thẻ sản phẩm thông minh:** Khi bấm "Nhắn tin" từ trang sản phẩm, tin nhắn tự động đính kèm thẻ xem trước (ảnh, tên, giá bán) để hai bên dễ dàng trao đổi.
- **Gợi ý tin nhắn nhanh (Quick Replies):** "Sản phẩm này còn không ạ?", "Giá này có bớt thêm được không?", "Bạn có ship COD không?"...
- **Thương lượng giá kín đáo:** Đàm phán trực tiếp trên ứng dụng mà không cần để lộ số điện thoại cá nhân.
- **Tích hợp bộ lọc từ khóa cấm:** Tự động chặn các tin nhắn chứa ngôn từ thô tục hoặc số tài khoản lừa đảo.

#### 6. Chi tiết Sản phẩm & Chia sẻ (Native Share)

- **Thư viện ảnh sản phẩm:** Xem ảnh lớn, zoom trực quan, thông tin chi tiết về tình trạng món đồ (% mới).
- **Chia sẻ đa nền tảng:** Nút chia sẻ tích hợp Android Native Share Sheet (`Intent.ACTION_SEND`), dễ dàng gửi bài đăng sang Zalo, Facebook, Messenger, Telegram...
- **Yêu thích (Favorites):** Thêm hoặc bỏ yêu thích với hoạt ảnh mượt mà, lưu trữ trên Firebase.

#### 7. Báo Cáo Vi Phạm (Violation Report System)

- Người dùng khi phát hiện bài đăng lừa đảo, hàng giả, sai mô tả, hàng cấm có thể bấm **"Báo cáo bài viết"**.
- Chọn lý do báo cáo (_Lừa đảo, Hàng giả, Sai danh mục, Thông tin không đúng..._).
- Dữ liệu báo cáo được gửi ngay lập tức về trung tâm kiểm duyệt của Admin.

#### 8. Đánh Giá & Nhận Xét Người Bán (5-Star Reviews)

- Hệ thống đánh giá uy tín người bán với điểm số trung bình (ví dụ: `4.8 / 5.0`).
- Người mua có thể để lại số sao và nhận xét chi tiết sau khi giao dịch.
- Minh bạch hóa uy tín cá nhân, loại bỏ người bán kém chất lượng.

#### 9. Đăng Tin Thanh Lý Tiện Lợi & An Toàn

- Đăng ảnh từ Thư viện hoặc chụp trực tiếp từ Camera.
- Tự động tải ảnh lên dịch vụ đám mây tốc độ cao **Cloudinary**.
- Định dạng tiền tệ VNĐ thông minh khi người dùng gõ phím.
- Chọn địa chỉ bán hàng 3 cấp hành chính (_Tỉnh/Thành -> Quận/Huyện -> Phường/Xã_) đồng bộ tự động qua API.
- **Tích hợp kiểm duyệt tự động (`ProfanityFilter`):** Ngăn chặn ngay từ đầu các bài đăng có nội dung vi phạm, hàng cấm hoặc tài khoản lừa đảo.

#### 10. Các Tiện Ích Hỗ Trợ & An Toàn

- **Lịch sử giao dịch (Transaction History):** Theo dõi danh sách đơn hàng đã mua và tin đăng đã bán thành công.
- **Trung tâm trợ giúp & Mẹo an toàn (Help Center & Safety Tips):** Hướng dẫn giao dịch trực tiếp nơi công cộng, kiểm tra hàng secondhand trước khi trả tiền, phòng tránh lừa đảo chuyển cọc.
- **Giới thiệu ứng dụng (About App):** Thông tin phiên bản, điều khoản sử dụng, chính sách quyền riêng tư và cam kết phát triển cộng đồng.

---

### Dành Cho Quản Trị Viên (Admin Dashboard & Moderation)

Hệ thống quản trị mạnh mẽ, trực quan với 6 phân hệ quản lý:

- **Biểu Đồ Thống Kê & Phân Tích Trực Quan (Analytics & Visual Charts):**
  - **Biểu đồ cột tần suất đăng bài:** Biểu đồ cột trực quan thể hiện số lượng bài đăng mới theo ngày trong tuần (T2 - CN trong 7 ngày gần nhất).
  - **Biểu đồ tỷ lệ danh mục hot nhất:** Xếp hạng các danh mục thịnh hành nhất (#1, #2, #3...) kèm phần trăm thị phần và thanh tiến trình màu sắc.
  - **Biểu đồ tỷ lệ tài khoản:** Thanh tỷ lệ trực quan phân bổ giữa tài khoản hoạt động bình thường và tài khoản bị xử lý vi phạm.
  - **Số liệu tổng hợp:** Tổng số bài đăng, bài đang hiển thị, bài đã hoàn thành giao dịch (đã bán), bài bị ẩn vi phạm, tổng thành viên và quản trị viên.
- **Quản Lý Banners Quảng Cáo Động (Dynamic Banner Management):**
  - Quản lý danh sách banner trực tiếp từ ứng dụng thay vì cố định trong mã nguồn.
  - Thêm banner mới: Tải link ảnh trực tuyến, đặt tiêu đề, phụ đề và liên kết đến danh mục hoặc bài viết khuyến mãi nổi bật.
  - Bật/tắt trạng thái hiển thị tức thì với công tắc Switch hoặc xóa banner hết hạn.
  - Tự động hiển thị và đồng bộ realtime ra Banner Carousel trên Trang chủ người dùng.
- **Quản Lý Danh Mục Sản Phẩm Trực Tuyến (Dynamic Categories):**
  - Admin tạo thêm danh mục mới (ví dụ: Thú cưng & Phụ kiện, Nhạc cụ, Đồ gia dụng thông minh...) ngay trên ứng dụng.
  - Dữ liệu lưu vào Firebase Realtime Database và tự động hiển thị ra Trang chủ cũng như Màn hình chọn danh mục đăng bán mà không cần cập nhật lại app.
  - Hỗ trợ xóa danh mục trực tuyến khi không còn nhu cầu sử dụng.
- **Khóa Tài Khoản Có Thời Hạn & Kèm Lý Do (Temporary Ban & Ban Reason):**
  - **Phân cấp 4 mức xử phạt:**
    - Cảnh cáo: Gửi thông báo cảnh cáo vi phạm vào Trung tâm thông báo người dùng.
    - Khóa đăng bài 3 ngày kèm lý do vi phạm.
    - Khóa tài khoản 7 ngày kèm lý do vi phạm.
    - Khóa tài khoản vĩnh viễn đối với hành vi vi phạm nghiêm trọng.
  - **Thông báo rõ ràng khi đăng nhập:** Khi tài khoản bị khóa thực hiện đăng nhập, hệ thống sẽ chặn và hiển thị hộp thoại: _"Tài khoản của bạn tạm thời bị khóa đến ngày [Thời gian] vì lý do: [Lý do vi phạm]"_.
  - **Tự động mở khóa (Auto-unban):** Hệ thống tự động kiểm tra thời hạn và mở khóa tài khoản khi hết hạn phạt.
  - **Quản lý danh sách thành viên:** Phân loại theo 3 bộ lọc: _"Tất cả"_, _"Hoạt động"_, _"Đã khóa"_, hỗ trợ mở khóa thủ công khi người dùng khiếu nại.
- **Bộ lọc tự động từ khóa cấm & Danh sách đen (Auto Profanity Filter):**
  - Module `ProfanityFilter.kt` tự động quét và chuẩn hóa tiếng Việt không dấu, loại bỏ khoảng trắng ngụy trang.
  - Chặn ngay lập tức:
    - Danh sách đen các số tài khoản ngân hàng chuyên lừa đảo cọc.
    - Danh mục hàng hóa cấm giao dịch: Vũ khí quân dụng, súng đạn, chất gây nghiện, ma túy, tiền giả, bằng lái giả, động vật quý hiếm...
    - Từ ngữ cờ bạc, cá độ, lô đề, tài xỉu.
    - Từ ngữ thô tục, xúc phạm danh dự.
- **Xử lý Báo cáo vi phạm:**
  - Danh sách bài đăng bị gắn cờ cảnh báo vi phạm từ cộng đồng.
  - Xem chi tiết lý do người dùng gửi lên.
  - Quyết định xóa bài ngay lập tức hoặc gỡ cờ cảnh báo.
- **Kiểm duyệt bài đăng toàn sàn:**
  - Xem và tìm kiếm bất kỳ bài đăng nào theo tên, người bán, danh mục.
  - Tạm ẩn bài viết nghi vấn hoặc khôi phục hiển thị.
  - Xóa vĩnh viễn bài đăng vi phạm khỏi Realtime Database và Cloud Firestore.
- **Quản lý phân quyền thành viên:**
  - Cấp quyền hoặc thu hồi quyền Admin với 1 chạm.
  - Huy hiệu phân biệt rõ ràng: `ADMIN` (Vàng kim) và `MEMBER` (Thành viên).

#### Cách kích hoạt quyền Admin:

1. **Đăng ký tài khoản có tiền tố `admin@`** (Ví dụ: `admin@gmail.com`, mật khẩu tùy ý). Ứng dụng tự động kích hoạt quyền Admin.
2. **Hoặc phân quyền thủ công trên Firebase Console:** Trong Firestore collection `users/{userId}`, gán thuộc tính `isAdmin: true` (kiểu boolean).

---

## 2. Kiến Trúc & Ngăn Xếp Công Nghệ (Tech Stack)

Dự án áp dụng mô hình chuẩn công nghiệp **MVVM (Model - View - ViewModel)** kết hợp **Feature-First Architecture**:

| Hạng mục           | Công nghệ / Thư viện                     | Phiên bản       | Mục đích sử dụng                                              |
| :----------------- | :--------------------------------------- | :-------------- | :------------------------------------------------------------ |
| **Ngôn ngữ**       | Kotlin                                   | 2.0.21          | Ngôn ngữ phát triển Android chuẩn hiện đại                    |
| **Build Tool**     | Android Gradle Plugin (AGP)              | 8.13.2          | Hệ thống build dự án chuẩn                                    |
| **Gradle**         | Gradle Wrapper                           | 8.13            | Hệ thống tự động hóa build                                    |
| **Môi trường JVM** | Java Target / Source                     | Java 21 (LTS)   | Tương thích tối ưu AGP 8.x & Gradle 8.13                      |
| **UI Framework**   | Jetpack Compose (BOM)                    | 2025.02.00      | Xây dựng giao diện khai báo hiện đại                          |
| **Design System**  | Material Design 3                        | 1.8.2           | Thành phần giao diện chuẩn Google                             |
| **Điều hướng**     | Navigation Compose                       | 2.8.9           | Quản lý chuyển trang mượt mà                                  |
| **Xác thực**       | Firebase Authentication & Google Sign-In | 23.2.0          | Đăng nhập tài khoản & xác thực Google                         |
| **Cơ sở dữ liệu**  | Firebase Realtime DB & Cloud Firestore   | 21.0.0 / 25.1.4 | Dữ liệu sản phẩm, chat realtime, thông báo, báo cáo, đánh giá |
| **Media Cloud**    | Cloudinary & Coil Compose                | 1.7.0 / 2.7.0   | Upload ảnh đám mây & nạp ảnh bất đồng bộ                      |
| **Networking**     | Retrofit 2 & Gson Converter              | 2.11.0          | Gọi API địa giới hành chính Việt Nam                          |
| **Dữ liệu nền**    | Room Database & WorkManager              | 2.6.1 / 2.10.1  | Hỗ trợ lưu trữ offline & tác vụ nền                           |

---

## 3. Cấu Trúc Thư Mục Dự Án

```text
AppMuaBanDoCu/app/src/main/java/com/example/appmuabandocu/
├── MainActivity.kt                      # Single Activity chính khởi chạy Compose App
├── api/                                 # Cấu hình Retrofit & Web API
│   ├── ApiService.kt                    # Endpoint API hành chính VN
│   └── RetrofitInstance.kt              # Retrofit Client
├── component/                           # UI Component dùng chung
│   └── FeatureInDevelopmentDialog.kt    # Hộp thoại tính năng
├── core/                                # Cốt lõi điều hướng & màn hình mở đầu
│   ├── navigation/
│   │   ├── BottomNavigation.kt          # Thanh Bottom Bar theo bảng màu 60-30-10
│   │   ├── MyAppNavigation.kt           # NavHost điều hướng, tự động đăng xuất tài khoản bị ban
│   │   └── model/Screen.kt              # Định nghĩa Sealed Class các màn hình (Notifications, Chat...)
│   └── ui/
│       ├── SplashRoleScreen.kt          # Màn hình chọn luồng trải nghiệm
│       └── SplashScreen.kt              # Màn hình Splash Logo thương hiệu
├── data/                                # Xử lý dữ liệu & media upload
│   ├── Address.kt                       # Model cấp hành chính (Tỉnh, Huyện, Xã)
│   └── CloudinaryUploader.kt            # Coroutine upload ảnh qua OkHttp
├── feature_add_product/                 # Tính năng Đăng bán sản phẩm
│   ├── CurrencyInputTransformation.kt   # Định dạng tiền tệ VNĐ realtime
│   └── ui/
│       ├── AddProductScreen.kt          # Giao diện đăng tin bán hàng (tích hợp ProfanityFilter & Ban Check)
│       └── CategoryScreen.kt            # Giao diện chọn danh mục
├── feature_admin/                       # Bảng Điều Khiển Quản Trị Viên (Admin Dashboard)
│   └── AdminDashboardScreen.kt          # Kiểm duyệt bài, xử lý cờ vi phạm, Khóa/Mở khóa tài khoản (Ban/Unban)
├── feature_auth/                        # Xác thực tài khoản
│   ├── LoginScreen.kt                   # Màn hình Đăng nhập (Email & Google)
│   └── RegisterMainScreen.kt            # Màn hình Đăng ký tài khoản
├── feature_chat/                        # Nhắn tin trực tiếp Người mua & Người bán
│   ├── ChatListScreen.kt                # Danh sách các cuộc trò chuyện
│   └── ChatScreen.kt                    # Phòng chat realtime (chặn từ khóa cấm & chặn user bị khóa)
├── feature_favorite/                    # Danh sách sản phẩm yêu thích
│   └── FavoriteScreen.kt
├── feature_home/                        # Trang chủ & Tìm kiếm lọc nâng cao
│   └── HomeScreen.kt                    # Lịch sử tìm kiếm Chips, Đồ cũ gần tôi, Chuông thông báo
├── feature_mxh/                         # Dạo chợ đồ cũ (Feed mạng xã hội)
│   └── MxhScreen.kt                     # Bảng tin dọc, gọi điện thoại trực tiếp
├── feature_notification/                # Trung tâm thông báo In-App
│   └── NotificationsScreen.kt           # Màn hình danh sách thông báo đa phân loại thời gian thực
├── feature_product/                     # Chi tiết sản phẩm & Báo cáo
│   └── ProductDetailScreen.kt           # Slider ảnh, cờ báo cáo, nút chat gắn thẻ sản phẩm, chia sẻ
├── feature_profile/                     # Tài khoản cá nhân & tiện ích
│   ├── AboutAppScreen.kt                # Giới thiệu ứng dụng & điều khoản
│   ├── HelpCenterScreen.kt              # Trung tâm trợ giúp & Mẹo an toàn mua bán
│   ├── ManageProductScreen.kt           # Quản lý bài đăng cá nhân (tích hợp nút Đẩy Tin)
│   ├── ProfileDetailScreen.kt           # Chỉnh sửa thông tin cá nhân
│   ├── ProfileScreen.kt                 # Hồ sơ tài khoản, nút Admin, lối vào tiện ích
│   ├── ProfileUserScreen.kt             # Xem hồ sơ người bán khác
│   ├── ReviewsScreen.kt                 # Xem đánh giá 5 sao & nhận xét người bán
│   └── TransactionHistoryScreen.kt      # Lịch sử mua hàng & bán hàng
├── model/                               # Data Models
│   ├── AppNotification.kt               # Model thông báo thời gian thực
│   ├── ChatConversation.kt             # Model danh sách hội thoại
│   ├── ChatMessage.kt                  # Model tin nhắn (hỗ trợ đính kèm sản phẩm)
│   ├── Product.kt                       # Model sản phẩm (hỗ trợ lastBumpedAt)
│   ├── Report.kt                        # Model báo cáo bài viết vi phạm
│   ├── Review.kt                        # Model đánh giá người bán (5 sao)
│   └── User.kt                          # Model người dùng (hỗ trợ isBanned & banReason)
├── repository/                          # Data Repositories
│   ├── AuthRepository.kt                # Quản lý xác thực Firebase
│   ├── ProductRepository.kt             # Quản lý dữ liệu sản phẩm
│   └── UserRepository.kt                # Quản lý hồ sơ người dùng
├── ui/theme/                            # Hệ thống Theme & Màu sắc 60-30-10
│   ├── Color.kt                         # Định nghĩa mã màu Brand Navy, White, Coral Orange
│   ├── Theme.kt                         # Cấu hình MaterialTheme ColorScheme
│   └── Type.kt                          # Cấu hình Typography
├── utils/                               # Tiện ích dùng chung
│   ├── NetworkConnectivityObserver.kt   # Theo dõi kết nối mạng
│   ├── ProfanityFilter.kt               # Bộ lọc tự động từ khóa cấm, số tài khoản lừa đảo, hàng cấm
│   └── TimeUtils.kt                     # Định dạng thời gian tương đối
└── viewmodel/                           # State Holders & Business Logic
    ├── AddressViewModel.kt
    ├── AdminViewModel.kt                # Xử lý duyệt bài, user, Khóa/Mở khóa tài khoản, gửi thông báo
    ├── AuthViewModel.kt                 # Xử lý đăng nhập, đăng ký, Google Sign-In
    ├── ChatViewModel.kt                 # Xử lý phòng chat & tin nhắn realtime
    ├── FavoriteViewModel.kt             # Xử lý danh sách yêu thích
    ├── ManageProductViewModel.kt        # Quản lý bài đăng & Xử lý Đẩy tin 24h
    ├── NotificationViewModel.kt         # Xử lý tải và cập nhật thông báo realtime
    ├── ProductViewModel.kt              # Lọc sản phẩm, slider giá, tìm kiếm
    ├── ProfileUserViewModel.kt          # Xử lý xem hồ sơ người khác
    ├── ProfileViewModel.kt              # Xử lý cập nhật thông tin cá nhân (lắng nghe trạng thái bị khóa)
    └── ReviewViewModel.kt               # Xử lý gửi và tải đánh giá 5 sao
```

---

## 4. Hướng Dẫn Thiết Lập Firebase Chi Tiết (Firebase Setup Guide)

### Bước 1: Tạo dự án Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/) và đăng nhập tài khoản Google.
2. Nhấn **Add project** -> Đặt tên dự án (ví dụ: `AppMuaBanDoCu`) -> Nhấn **Create project**.

### Bước 2: Đăng ký ứng dụng Android & Tải `google-services.json`

1. Tại trang tổng quan dự án Firebase, chọn biểu tượng **Android**.
2. Nhập chính xác thông tin:
   - **Android package name:** `com.example.appmuabandocu`
   - **App nickname (tùy chọn):** `Cho Do Cu`
   - **Debug signing certificate SHA-1:**
     - Mở Terminal trong Android Studio và chạy lệnh:
       ```bash
       .\gradlew.bat signingReport
       ```
     - Sao chép mã **SHA1** của variant `debug` và dán vào ô này (bắt buộc để tính năng Google Sign-In hoạt động).
3. Nhấn **Register app** -> Tải tệp **`google-services.json`** về máy.
4. Đặt tệp vào đúng thư mục:
   ```text
   AppMuaBanDoCu/app/google-services.json
   ```

### Bước 3: Bật Xác thực (Firebase Authentication)

1. Trong menu bên trái, vào **Build** -> **Authentication** -> Nhấn **Get started**.
2. Tab **Sign-in method**:
   - **Email/Password:** Bật **Enable** -> Nhấn **Save**.
   - **Google:**
     - Bật **Enable**, chọn email hỗ trợ dự án (_Project support email_).
     - Nhấn vào **Web SDK configuration** -> Sao chép chuỗi **Web client ID**.
     - Nhấn **Save**.
3. Cập nhật Web Client ID vào mã nguồn tại [`strings.xml`](AppMuaBanDoCu/app/src/main/res/values/strings.xml):
   ```xml
   <resources>
       <string name="app_name">AppMuaBanDoCu</string>
       <string name="web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
   </resources>
   ```

### Bước 4: Khởi tạo Cloud Firestore Database

1. Trong menu bên trái, vào **Build** -> **Firestore Database** -> Nhấn **Create database**.
2. Chọn vị trí máy chủ: Khuyên dùng `asia-southeast1` (Singapore) để có tốc độ phản hồi nhanh nhất.
3. Chọn chế độ **Start in test mode** -> Nhấn **Create**.
4. Cập nhật quy tắc bảo mật tại tab **Rules**:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
   _Nhấn **Publish** để lưu lại._

### Bước 5: Khởi tạo Realtime Database

1. Trong menu bên trái, vào **Build** -> **Realtime Database** -> Nhấn **Create Database**.
2. Chọn vị trí lưu trữ (ví dụ: `Singapore (asia-southeast1)` hoặc `United States`).
3. Chọn **Start in test mode** -> Nhấn **Enable**.
4. Cập nhật quy tắc bảo mật tại tab **Rules**:
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```
   _Nhấn **Publish** để lưu lại._

### Bước 6: Cấu hình lưu trữ đám mây Cloudinary (Tùy chọn)

Ứng dụng sử dụng dịch vụ đám mây Cloudinary để tải ảnh sản phẩm và ảnh đại diện tốc độ cao. Bạn có thể sử dụng cấu hình mặc định sẵn có hoặc cấu hình tài khoản riêng của mình tại tệp [`CloudinaryUploader.kt`](AppMuaBanDoCu/app/src/main/java/com/example/appmuabandocu/data/CloudinaryUploader.kt):

1. Đăng ký tài khoản miễn phí tại [Cloudinary](https://cloudinary.com/).
2. Trong Dashboard, lấy **Cloud Name** của bạn.
3. Vào **Settings** -> **Upload** -> Mục **Upload presets** -> Chọn **Add upload preset**:
   - Signing Mode: Chọn **Unsigned** (cho phép upload trực tiếp từ app Android).
   - Đặt tên preset (ví dụ: `unsigned_upload`).
4. Cập nhật Cloud Name và Upload Preset vào mã nguồn tệp `CloudinaryUploader.kt`:
   ```kotlin
   .addFormDataPart("upload_preset", "YOUR_UPLOAD_PRESET")
   .url("https://api.cloudinary.com/v1_1/YOUR_CLOUD_NAME/image/upload")
   ```

---

## 5. Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### Yêu Cầu Môi Trường

- **Android Studio:** Ladybug (2024.2.1+), Koala hoặc Hedgehog.
- **JDK:** Java 21 (OpenJDK 21 LTS).
- **Thiết bị:** Android 8.0 (API level 26) trở lên.

### Các Bước Cài Đặt:

1. **Clone repository về máy:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/AppMuaBanDoCu.git
   cd AppMuaBanDoCu
   ```
2. **Cấu hình Firebase an toàn:**
   - Hoàn tất các bước thiết lập Firebase ở Mục 4 ở trên.
   - Đặt tệp `google-services.json` vào thư mục `AppMuaBanDoCu/app/google-services.json`.
3. **Mở dự án trong Android Studio:**
   - Mở Android Studio -> Chọn **Open** -> Điều hướng tới thư mục `AppMuaBanDoCu`.
   - Chờ Android Studio đồng bộ Gradle (Gradle Sync) hoàn tất.
4. **Chạy ứng dụng:**
   - Khởi chạy máy ảo Android (Emulator) hoặc kết nối điện thoại thật (bật _Gỡ lỗi USB / USB Debugging_).
   - Nhấn nút **Run** (phím tắt `Shift + F10`).

---

## Giấy Phép (License)

Dự án được phân phối dưới giấy phép mã nguồn mở **MIT License**. Bạn được tự do học tập, nghiên cứu và phát triển tiếp nối dự án.
