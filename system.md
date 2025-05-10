# System Documentation

## 1. Use Case Diagrams

### Authentication & Profile System

```mermaid
graph TD
    User((User))

    subgraph Authentication
    Login[Login]
    Register[Register]
    Logout[Logout]
    ValidateEmail[Validate Email]
    SocialLogin[Social Media Login]
    end

    subgraph Profile Management
    EditProfile[Edit Profile]
    UpdateAvatar[Update Avatar]
    ManageSettings[Manage Settings]
    CustomizePreferences[Customize Preferences]
    end

    User --> Login
    User --> Register
    User --> Logout
    User --> SocialLogin
    Register --> ValidateEmail
    User --> EditProfile
    EditProfile --> UpdateAvatar
    User --> ManageSettings
    ManageSettings --> CustomizePreferences
```

### Social Features

```mermaid
graph TD
    User((User))

    subgraph Friend Management
    SendRequest[Send Friend Request]
    AcceptRequest[Accept Friend Request]
    DeclineRequest[Decline Friend Request]
    CancelRequest[Cancel Friend Request]
    ViewFriends[View Friends List]
    SearchUsers[Search Users]
    end

    subgraph Chat System
    SendMessage[Send Message]
    ViewMessages[View Messages]
    ReceiveNotification[Receive Chat Notification]
    ManageChats[Manage Chat Threads]
    end

    User --> SendRequest
    User --> AcceptRequest
    User --> DeclineRequest
    User --> CancelRequest
    User --> ViewFriends
    User --> SearchUsers
    User --> SendMessage
    User --> ViewMessages
    User --> ManageChats
    SendMessage --> ReceiveNotification
```

### Content Management

```mermaid
graph TD
    User((User))

    subgraph Post Management
    CreatePost[Create Post]
    AddLocation[Add Location]
    AddImage[Add Image]
    EditPost[Edit Post]
    DeletePost[Delete Post]
    ViewFeed[View News Feed]
    end

    subgraph Interaction
    LikePost[Like Post]
    CommentPost[Comment]
    SharePost[Share Post]
    SavePost[Save Post]
    end

    subgraph Media
    UploadImage[Upload Image]
    ProcessImage[Process Image]
    StoreMedia[Store in Cloudinary]
    end

    User --> CreatePost
    CreatePost --> AddLocation
    CreatePost --> AddImage
    AddImage --> UploadImage
    UploadImage --> ProcessImage
    ProcessImage --> StoreMedia
    User --> EditPost
    User --> DeletePost
    User --> ViewFeed
    User --> LikePost
    User --> CommentPost
    User --> SharePost
    User --> SavePost
```

### Location & Notification System

```mermaid
graph TD
    User((User))

    subgraph Location Services
    ViewMap[View Map]
    SelectLocation[Select Location]
    TagLocation[Tag Location in Post]
    UpdateUserLocation[Update User Location]
    end

    subgraph Notifications
    ViewNotifications[View Notifications]
    ManageChannels[Manage Channels]
    ConfigurePrefs[Configure Preferences]
    ReceivePush[Receive Push Notifications]
    end

    User --> ViewMap
    User --> SelectLocation
    User --> TagLocation
    User --> UpdateUserLocation
    User --> ViewNotifications
    User --> ManageChannels
    User --> ConfigurePrefs
    ManageChannels --> ReceivePush
```

## 2. Entity Relationship Diagram

```mermaid
erDiagram
    User ||--o{ Post : "creates"
    User ||--o{ ChatMessage : "sends"
    User ||--o{ FriendRequest : "sends/receives"
    User ||--o{ User : "friends"
    User ||--o{ Notification : "receives"
    Post ||--o{ Comment : "has"
    Post ||--o{ Like : "has"

    User {
        string id PK
        string username
        string email
        string fullName
        string phoneNumber
        string gender
        string location
        string birthdate
        string age
        map friends
        string avatarUrl
        string fcmToken
        json preferences
        timestamp lastActive
    }

    Post {
        string id PK
        string userId FK
        string content
        string imageUrl
        long timestamp
        int likes
        int comments
        double latitude
        double longitude
        map likedUsers
        boolean isPublic
        timestamp lastEdited
    }

    ChatMessage {
        string id PK
        string senderId FK
        string receiverId FK
        string message
        long timestamp
        boolean isRead
        string messageType
        string mediaUrl
    }

    FriendRequest {
        string id PK
        string senderId FK
        string receiverId FK
        enum status
        timestamp sentAt
        timestamp respondedAt
    }

    Notification {
        string id PK
        string userId FK
        string title
        string message
        string type
        string targetId
        long timestamp
        boolean isRead
        string priority
    }

    Comment {
        string id PK
        string postId FK
        string userId FK
        string content
        timestamp createdAt
        int likes
    }

    Like {
        string id PK
        string postId FK
        string userId FK
        timestamp createdAt
    }
```

## System Features

1. **Authentication & Security**

   - Email/Password authentication
   - Social media login integration
   - Session management
   - Token-based authentication
   - Security rules implementation

2. **Profile System**

   - Comprehensive user profiles
   - Avatar management with Cloudinary
   - Profile customization
   - Privacy settings
   - User preferences

3. **Social Networking**

   - Friend management system
   - Real-time chat
   - Post creation and interaction
   - News feed with real-time updates
   - Comment and like system

4. **Media Management**

   - Image upload and processing
   - Cloudinary integration
   - Media optimization
   - Cache management
   - Lazy loading implementation

5. **Location Services**

   - MapBox integration
   - Location tagging
   - Map visualization
   - Location-based features
   - Geolocation services

6. **Notifications**

   - Firebase Cloud Messaging
   - Multiple notification channels
   - Push notification system
   - Notification preferences
   - Real-time alerts

7. **Data Management**

   - Firebase Realtime Database
   - Offline persistence
   - Data synchronization
   - Cache strategies
   - Real-time updates

8. **UI/UX Features**
   - Material Design implementation
   - Custom animations
   - Responsive layouts
   - Interactive elements
   - Loading states with Shimmer
