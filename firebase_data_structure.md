# Firebase Database Structure - NMIMS Canteen

## Collections Structure

### 1. Users Collection
```javascript
/users/{userId}
{
  "email": "user@example.com",
  "name": "John Doe",
  "phone": "+1234567890",
  "role": "customer", // customer, admin
  "addresses": [
    {
      "type": "home",
      "street": "123 Main St",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "isDefault": true
    }
  ],
  "preferences": {
    "vegetarian": true,
    "notifications": true,
    "language": "english"
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "isActive": true
}
```

### 2. FoodItems Collection (With User's Provided Items)
```javascript
/foodItems/{itemId}
{
  "name": "Aloo Paratha",
  "description": "Crispy and delicious potato-stuffed flatbread served with butter and curd",
  "category": "Indian Breakfast",
  "price": 60.00,
  "imageUrl": "https://picsum.photos/food_items/aloo_paratha.jpg",
  "vegetarian": true,
  "available": true,
  "rating": 4.5,
  "ratingCount": 150,
  "preparationTime": 15,
  "ingredients": ["Potatoes", "Wheat Flour", "Spices", "Butter"],
  "inventory": {
    "stockQuantity": 50,
    "lowStockThreshold": 10,
    "batchNumber": "B001",
    "expiryDate": "2024-01-05"
  },
  "salesData": {
    "totalSold": 1250,
    "revenue": 75000.00,
    "lastOrdered": "2024-01-01T12:30:00Z"
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "isActive": true
}

// Additional Food Items (from user's list):
{
  "itemId": "bread_pakora",
  "name": "Bread Pakora",
  "description": "Crispy bread fritters stuffed with spicy potato filling",
  "category": "Snacks",
  "price": 40.00,
  "imageUrl": "https://picsum.photos/food_items/bread_pakora.jpg",
  "vegetarian": true,
  "preparationTime": 12
},
{
  "itemId": "burger",
  "name": "Veg Burger",
  "description": "Juicy vegetable patty with fresh lettuce, tomato, and special sauce",
  "category": "Fast Food",
  "price": 80.00,
  "imageUrl": "https://picsum.photos/food_items/burger.jpg",
  "vegetarian": true,
  "preparationTime": 10
},
{
  "itemId": "cheese_sandwich",
  "name": "Cheese Sandwich",
  "description": "Grilled cheese sandwich with melted cheese and vegetables",
  "category": "Snacks",
  "price": 50.00,
  "imageUrl": "https://picsum.photos/food_items/cheese_sandwich.jpg",
  "vegetarian": true,
  "preparationTime": 8
},
{
  "itemId": "chole_bhature",
  "name": "Chole Bhature",
  "description": "Spicy chickpea curry served with fluffy fried bread",
  "category": "North Indian",
  "price": 90.00,
  "imageUrl": "https://picsum.photos/food_items/chole_bhature.jpg",
  "vegetarian": true,
  "preparationTime": 20
},
{
  "itemId": "chole_kulche",
  "name": "Chole Kulche",
  "description": "Spicy chickpea curry served with soft leavened bread",
  "category": "North Indian",
  "price": 70.00,
  "imageUrl": "https://picsum.photos/food_items/chole_kulche.jpg",
  "vegetarian": true,
  "preparationTime": 15
},
{
  "itemId": "idli_sambar",
  "name": "Idli Sambar",
  "description": "Soft steamed rice cakes served with lentil soup",
  "category": "South Indian",
  "price": 60.00,
  "imageUrl": "https://picsum.photos/food_items/idli_sambar.jpg",
  "vegetarian": true,
  "preparationTime": 12
},
{
  "itemId": "khamand",
  "name": "Khamand",
  "description": "Soft and spongy steamed savory cake with sweet and spicy chutney",
  "category": "Gujarati",
  "price": 50.00,
  "imageUrl": "https://picsum.photos/food_items/khamand.jpg",
  "vegetarian": true,
  "preparationTime": 15
},
{
  "itemId": "masala_dosa",
  "name": "Masala Dosa",
  "description": "Crispy rice crepe filled with spiced potato mixture",
  "category": "South Indian",
  "price": 80.00,
  "imageUrl": "https://picsum.photos/food_items/masala_dosa.jpg",
  "vegetarian": true,
  "preparationTime": 18
},
{
  "itemId": "paneer_paratha",
  "name": "Paneer Paratha",
  "description": "Flatbread stuffed with cottage cheese and spices",
  "category": "Indian Breakfast",
  "price": 90.00,
  "imageUrl": "https://picsum.photos/food_items/paneer_paratha.png",
  "vegetarian": true,
  "preparationTime": 18
},
{
  "itemId": "pav_bhaji",
  "name": "Pav Bhaji",
  "description": "Spicy mashed vegetable curry served with buttered bread rolls",
  "category": "Maharashtrian",
  "price": 100.00,
  "imageUrl": "https://picsum.photos/food_items/pav_bhaji.jpg",
  "vegetarian": true,
  "preparationTime": 20
},
{
  "itemId": "pizza",
  "name": "Veg Pizza",
  "description": "Classic margherita pizza with fresh vegetables and cheese",
  "category": "Italian",
  "price": 150.00,
  "imageUrl": "https://picsum.photos/food_items/pizza.jpg",
  "vegetarian": true,
  "preparationTime": 25
},
{
  "itemId": "pizza2",
  "name": "Special Pizza",
  "description": "Gourmet pizza with premium vegetables and extra cheese",
  "category": "Italian",
  "price": 200.00,
  "imageUrl": "https://picsum.photos/food_items/pizza2.jpg",
  "vegetarian": true,
  "preparationTime": 30
},
{
  "itemId": "rava_dosa",
  "name": "Rava Dosa",
  "description": "Crispy semolina crepe with onion and spices",
  "category": "South Indian",
  "price": 70.00,
  "imageUrl": "https://picsum.photos/food_items/rava_dosa.jpg",
  "vegetarian": true,
  "preparationTime": 15
},
{
  "itemId": "red_pasta",
  "name": "Red Pasta",
  "description": "Pasta in tangy tomato sauce with vegetables",
  "category": "Italian",
  "price": 120.00,
  "imageUrl": "https://picsum.photos/food_items/red_pasta.jpg",
  "vegetarian": true,
  "preparationTime": 20
},
{
  "itemId": "samosa",
  "name": "Samosa",
  "description": "Crispy triangular pastry filled with spiced potatoes and peas",
  "category": "Snacks",
  "price": 20.00,
  "imageUrl": "https://picsum.photos/food_items/samosa.jpg",
  "vegetarian": true,
  "preparationTime": 10
},
{
  "itemId": "vada_pav",
  "name": "Vada Pav",
  "description": "Spicy potato fritter sandwich in a bread bun",
  "category": "Maharashtrian",
  "price": 30.00,
  "imageUrl": "https://picsum.photos/food_items/vada_pav.jpg",
  "vegetarian": true,
  "preparationTime": 8
},
{
  "itemId": "veg_sandwich",
  "name": "Veg Sandwich",
  "description": "Fresh vegetables sandwich with chutney and cheese",
  "category": "Snacks",
  "price": 45.00,
  "imageUrl": "https://picsum.photos/food_items/veg_sandwich.jpg",
  "vegetarian": true,
  "preparationTime": 8
},
{
  "itemId": "white_pasta",
  "name": "White Pasta",
  "description": "Creamy pasta with white sauce and vegetables",
  "category": "Italian",
  "price": 130.00,
  "imageUrl": "https://picsum.photos/food_items/white_pasta.jpg",
  "vegetarian": true,
  "preparationTime": 20
}
```

### 3. Orders Collection
```javascript
/orders/{orderId}
{
  "orderId": "ORD_20240101_001",
  "userId": "user123",
  "items": [
    {
      "foodItemId": "aloo_paratha",
      "name": "Aloo Paratha",
      "quantity": 2,
      "unitPrice": 60.00,
      "totalPrice": 120.00,
      "status": "confirmed",
      "preparationTime": 15
    }
  ],
  "subtotal": 120.00,
  "deliveryCharges": 40.00,
  "tax": 12.80,
  "totalAmount": 172.80,
  "status": "confirmed", // pending, confirmed, preparing, ready, delivered, cancelled
  "paymentMethod": "cash", // cash, upi, card
  "paymentStatus": "pending", // pending, completed, failed
  "deliveryType": "pickup", // pickup, delivery
  "deliveryAddress": {
    "street": "123 Main St",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001"
  },
  "estimatedDeliveryTime": "2024-01-01T13:15:00Z",
  "actualDeliveryTime": null,
  "createdAt": "2024-01-01T12:30:00Z",
  "updatedAt": "2024-01-01T12:35:00Z",
  "notes": "Extra butter please"
}
```

### 4. Cart Collection
```javascript
/cart/{userId}
{
  "userId": "user123",
  "items": [
    {
      "foodItemId": "aloo_paratha",
      "name": "Aloo Paratha",
      "quantity": 2,
      "unitPrice": 60.00,
      "totalPrice": 120.00,
      "addedAt": "2024-01-01T12:25:00Z"
    }
  ],
  "subtotal": 120.00,
  "deliveryCharges": 40.00,
  "tax": 12.80,
  "totalAmount": 172.80,
  "updatedAt": "2024-01-01T12:30:00Z"
}
```

### 5. Reviews Collection
```javascript
/reviews/{reviewId}
{
  "orderId": "ORD_20240101_001",
  "userId": "user123",
  "foodItemId": "aloo_paratha",
  "rating": 5,
  "comment": "Amazing taste and perfectly cooked!",
  "helpfulVotes": 10,
  "totalVotes": 12,
  "moderationStatus": "approved", // pending, approved, rejected
  "createdAt": "2024-01-01T15:00:00Z",
  "updatedAt": "2024-01-01T15:00:00Z"
}
```

### 6. Categories Collection
```javascript
/categories/{categoryId}
{
  "name": "Indian Breakfast",
  "description": "Traditional Indian breakfast items",
  "icon": "https://picsum.photos/icons/breakfast.png",
  "isActive": true,
  "displayOrder": 1,
  "itemCount": 15
}
```

### 7. Analytics Collection
```javascript
/analytics/{date}
{
  "date": "2024-01-01",
  "metrics": {
    "totalOrders": 125,
    "totalRevenue": 12500.00,
    "averageOrderValue": 100.00,
    "uniqueCustomers": 85,
    "repeatCustomers": 40,
    "topSellingItems": [
      {"foodItemId": "aloo_paratha", "quantity": 45, "revenue": 2700.00},
      {"foodItemId": "samosa", "quantity": 60, "revenue": 1200.00}
    ]
  }
}
```

### 8. Inventory Collection
```javascript
/inventory/{itemId}
{
  "foodItemId": "aloo_paratha",
  "currentStock": 50,
  "lowStockThreshold": 10,
  "maxStock": 100,
  "reorderLevel": 15,
  "batchNumber": "B001",
  "expiryDate": "2024-01-05",
  "lastRestocked": "2024-01-01T08:00:00Z",
  "totalConsumed": 75,
  "wastage": 5,
  "updatedAt": "2024-01-01T12:30:00Z"
}
```

### 9. Notifications Collection
```javascript
/notifications/{notificationId}
{
  "title": "Low Stock Alert",
  "message": "Aloo Paratha is running low on stock (5 items remaining)",
  "type": "inventory_alert", // order_alert, inventory_alert, system
  "priority": "high", // low, medium, high
  "targetAudience": "admin", // all, customer, admin
  "data": {
    "foodItemId": "aloo_paratha",
    "currentStock": 5
  },
  "isRead": false,
  "createdAt": "2024-01-01T12:30:00Z",
  "expiresAt": "2024-01-01T18:30:00Z"
}
```

## Firebase Security Rules

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own profile
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // Food items - readable by all authenticated users, writable by admins
    match /foodItems/{itemId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // Orders - users can read/write their own orders, admins can read all
    match /orders/{orderId} {
      allow read, write: if request.auth != null &&
        resource.data.userId == request.auth.uid;
      allow read: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // Cart - users can only access their own cart
    match /cart/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Reviews - users can create reviews for their own orders
    match /reviews/{reviewId} {
      allow create: if request.auth != null &&
        request.resource.data.userId == request.auth.uid;
      allow read: if request.auth != null;
      allow update, delete: if request.auth != null &&
        (resource.data.userId == request.auth.uid ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin");
    }

    // Analytics and Inventory - admin only
    match /analytics/{date} {
      allow read, write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    match /inventory/{itemId} {
      allow read, write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // Categories - readable by all, writable by admins
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // Notifications - users can read their own, admins can manage all
    match /notifications/{notificationId} {
      allow read: if request.auth != null &&
        (resource.data.targetAudience == "all" ||
         resource.data.targetAudience == "customer" ||
         (resource.data.targetAudience == "admin" &&
          get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin"));
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }
  }
}
```

## Firebase Indexes

```javascript
// Indexes for efficient queries
{
  "indexes": [
    {
      "collectionGroup": "foodItems",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "category",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "name",
          "order": "ASCENDING"
        }
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "userId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "createdAt",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "status",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "createdAt",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "reviews",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "foodItemId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "rating",
          "order": "DESCENDING"
        }
      ]
    }
  ]
}
```