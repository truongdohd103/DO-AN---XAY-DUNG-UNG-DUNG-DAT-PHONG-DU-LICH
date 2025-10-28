// Firebase Complete Import Script - ChillStay All Models (50 Records Each)
// Script này sẽ import dữ liệu cho tất cả các model trong domain/model

const admin = require('firebase-admin');

// Kiểm tra Service Account Key
let serviceAccount;
try {
    serviceAccount = require('./serviceAccountKey.json');
    console.log('✅ Service Account Key found');
} catch (error) {
    console.log('❌ Service Account Key not found');
    console.log('📋 Hướng dẫn tạo Service Account Key:');
    console.log('1. Truy cập: https://console.firebase.google.com/');
    console.log('2. Chọn project: chillstay-fab78');
    console.log('3. Project Settings → Service accounts');
    console.log('4. Generate new private key');
    console.log('5. Lưu file với tên: serviceAccountKey.json');
    process.exit(1);
}

// Khởi tạo Firebase Admin SDK
try {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        projectId: 'chillstay-fab78'
    });
    console.log('✅ Firebase Admin SDK initialized');
} catch (error) {
    console.error('❌ Failed to initialize Firebase Admin SDK:', error.message);
    process.exit(1);
}

const db = admin.firestore();


resetAndSeedChillStay()

// Seed 20 hotels and rooms with new fields for ChillStay test
async function seedChillStaySample() {
    const admin = require('firebase-admin');
    const db = admin.firestore();

    const cities = [
        { city: 'Ho Chi Minh City', country: 'Vietnam' },
        { city: 'Hanoi', country: 'Vietnam' },
        { city: 'Da Nang', country: 'Vietnam' },
        { city: 'Nha Trang', country: 'Vietnam' },
        { city: 'Phu Quoc', country: 'Vietnam' },
        { city: 'Bangkok', country: 'Thailand' },
        { city: 'Singapore', country: 'Singapore' },
        { city: 'Kuala Lumpur', country: 'Malaysia' },
        { city: 'Jakarta', country: 'Indonesia' },
        { city: 'Manila', country: 'Philippines' },
        { city: 'Seoul', country: 'South Korea' },
        { city: 'Tokyo', country: 'Japan' },
        { city: 'Bali', country: 'Indonesia' },
        { city: 'Phuket', country: 'Thailand' },
        { city: 'Hong Kong', country: 'Hong Kong' },
        { city: 'Macau', country: 'Macau' },
        { city: 'Taipei', country: 'Taiwan' },
        { city: 'Sydney', country: 'Australia' },
        { city: 'Melbourne', country: 'Australia' },
        { city: 'Dubai', country: 'UAE' }
    ];
    
    const hotelImages = [
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=600&h=400&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=600&h=400&fit=crop&crop=center'
    ];
    
    const roomImages = [
        'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=500&h=300&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500&h=300&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=500&h=300&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=500&h=300&fit=crop&crop=center',
        'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=500&h=300&fit=crop&crop=center'
    ];
    const hotelIds = [];
    const roomIds = [];

    for (let i = 0; i < 20; i++) {
        const loc = cities[i % cities.length];
        const hotel = {
            name: `Sample Hotel ${i + 1}`,
            country: loc.country,
            city: loc.city,
            rating: 4 + (i % 10) / 10,
            numberOfReviews: 20 + i * 3,
            imageUrl: hotelImages[i],
            minPrice: null,
            photoCount: 0
        };

        const hotelRef = await db.collection('hotels').add(hotel);
        hotelIds.push(hotelRef.id);

        const prices = [120 + i * 5, 160 + i * 5];
        let minPrice = null;
        const roomTypes = [
            { type: 'Standard', name: 'Standard Room', size: 20, view: 'City', capacity: 2, price: prices[0] },
            { type: 'Deluxe', name: 'Deluxe Room', size: 28, view: 'River', capacity: 3, price: prices[1] },
            { type: 'Suite', name: 'Executive Suite', size: 45, view: 'Ocean', capacity: 4, price: prices[0] * 1.8 },
            { type: 'Presidential', name: 'Presidential Suite', size: 80, view: 'City', capacity: 6, price: prices[1] * 2.5 },
            { type: 'Family', name: 'Family Room', size: 35, view: 'Garden', capacity: 5, price: prices[0] * 1.3 }
        ];
        
        for (let j = 0; j < roomTypes.length; j++) {
            const roomType = roomTypes[j];
            minPrice = minPrice == null ? roomType.price : Math.min(minPrice, roomType.price);
            const room = {
                hotelId: hotelRef.id,
                type: roomType.type,
                price: roomType.price,
                imageUrl: roomImages[j % roomImages.length],
                isAvailable: j === 0 ? true : (i % 3 !== 0), // Make some rooms unavailable
                capacity: roomType.capacity,
                detail: {
                    name: roomType.name,
                    size: roomType.size,
                    view: roomType.view
                }
            };
            const roomRef = await db.collection('rooms').add(room);
            roomIds.push(roomRef.id);
        }

        await db.collection('hotels').doc(hotelRef.id).set({ minPrice: minPrice, photoCount: 5 }, { merge: true });
    }

    // Seed Auth users (Admin SDK) and Firestore user profiles
    const userIds = [];
    
    // Create test users
    for (let i = 0; i < 10; i++) {
        const email = `user${i + 1}@chillstay.com`;
        const password = 'password123';
        let uid;
        try {
            const created = await admin.auth().createUser({
                email,
                password,
                displayName: `User ${i + 1}`
            });
            uid = created.uid;
            console.log(`✅ Created auth user: ${email} (${uid})`);
        } catch (e) {
            if (e && e.code === 'auth/email-already-exists') {
                const existing = await admin.auth().getUserByEmail(email);
                uid = existing.uid;
                console.log(`ℹ️ Auth user exists: ${email} (${uid})`);
            } else {
                console.warn(`⚠️ Failed to create auth user ${email}:`, e.message || e);
                continue;
            }
        }
        await db.collection('users').doc(uid).set({
            email,
            password,
            fullName: `User ${i + 1}`,
            gender: i % 2 === 0 ? 'Male' : 'Female',
            photoUrl: '',
            dateOfBirth: new Date().toISOString().slice(0, 10)
        }, { merge: true });
        userIds.push(uid);
    }

    // Seed bookmarks for all users (more diverse data)
    if (userIds.length > 0) {
        // Each user bookmarks 3-8 random hotels
        for (let userIndex = 0; userIndex < userIds.length; userIndex++) {
            const bookmarkCount = 3 + (userIndex % 6); // 3-8 bookmarks per user
            const shuffledHotels = [...hotelIds].sort(() => 0.5 - Math.random());
            
            for (let i = 0; i < Math.min(bookmarkCount, hotelIds.length); i++) {
                await db.collection('bookmarks').add({
                    userId: userIds[userIndex],
                    hotelId: shuffledHotels[i],
                    createdAt: admin.firestore.Timestamp.now()
                });
            }
        }
        console.log(`✅ Created bookmarks for ${userIds.length} users`);
    } else {
        console.warn('⚠️ No auth users -> skipping bookmarks seed');
    }

    // Seed reviews for first 5 hotels (only if we have users)
    if (userIds.length > 0) {
        for (let i = 0; i < Math.min(5, hotelIds.length); i++) {
            await db.collection('reviews').add({
                userId: userIds[i % userIds.length],
                hotelId: hotelIds[i],
                rating: 4 + (i % 2),
                comment: 'Great stay!'
            });
        }
    } else {
        console.warn('⚠️ No auth users -> skipping reviews seed');
    }

    // Seed vouchers with new model structure
    const voucherIds = [];
    const voucherTitles = [
        "UP TO\n5% OFF",
        "UP TO\n8% OFF", 
        "UP TO\n10% OFF",
        "UP TO\n15% OFF",
        "UP TO\n20% OFF",
        "DOMESTIC DEALS",
        "INTERNATIONAL DEALS",
        "EARLY BIRD\nSPECIAL",
        "WEEKEND\nGETAWAY",
        "LUXURY\nEXPERIENCE",
        "FAMILY\nPACKAGE",
        "BUSINESS\nTRAVEL",
        "ROMANTIC\nESCAPE",
        "ADVENTURE\nDEAL",
        "CITY BREAK\nOFFER"
    ];
    
    const voucherDescriptions = [
        "Save up to $800 on hotel bookings",
        "Save up to $1,000 on hotel bookings", 
        "Save up to $1,500 on hotel bookings",
        "Save up to $2,000 on hotel bookings",
        "Save up to $2,500 on hotel bookings",
        "Enjoy special prices at local hotels and resorts",
        "Enjoy special prices at international hotels and resorts",
        "Book early and save more on your stay",
        "Perfect deals for weekend getaways",
        "Experience luxury at affordable prices",
        "Great deals for family vacations",
        "Special rates for business travelers",
        "Romantic packages for couples",
        "Adventure deals for thrill seekers",
        "City break offers for urban explorers"
    ];
    
    for (let i = 0; i < 15; i++) {
        const now = new Date();
        const validFrom = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000); // 7 days ago
        const validTo = new Date(now.getTime() + (30 + i * 5) * 24 * 60 * 60 * 1000); // 30-100 days from now
        
        const voucherRef = await db.collection('vouchers').add({
            code: `SAVE${10 + i}`,
            title: voucherTitles[i % voucherTitles.length],
            description: voucherDescriptions[i % voucherDescriptions.length],
            type: i % 3 === 0 ? 'FIXED_AMOUNT' : 'PERCENTAGE',
            value: i % 3 === 0 ? (50 + i * 10) : (5 + i % 15), // Fixed amount: $50-200, Percentage: 5-20%
            status: 'ACTIVE',
            validFrom: admin.firestore.Timestamp.fromDate(validFrom),
            validTo: admin.firestore.Timestamp.fromDate(validTo),
            applyForHotel: i % 4 === 0 ? hotelIds.slice(0, 3) : null, // Some vouchers apply to specific hotels
            conditions: {
                minBookingAmount: i % 2 === 0 ? (100 + i * 50) : 0,
                maxDiscountAmount: i % 3 === 0 ? (500 + i * 100) : 0,
                applicableForNewUsers: i % 5 === 0,
                applicableForExistingUsers: true,
                maxUsagePerUser: 1 + (i % 3),
                maxTotalUsage: 0, // Unlimited
                currentUsage: 0,
                requiredUserLevel: i % 6 === 0 ? 'VIP' : null,
                validDays: [],
                validTimeSlots: []
            },
            createdAt: admin.firestore.Timestamp.now(),
            updatedAt: admin.firestore.Timestamp.now()
        });
        voucherIds.push(voucherRef.id);
    }
    
    console.log(`✅ Created ${voucherIds.length} vouchers with new model structure`);

    // Seed multiple bookings with diverse data for testing
    if (userIds.length > 0 && roomIds.length > 0) {
        const bookingStatuses = ['PENDING', 'COMPLETED', 'CANCELLED'];
        const paymentMethods = ['CREDIT_CARD', 'DEBIT_CARD', 'DIGITAL_WALLET', 'BANK_TRANSFER', 'CASH'];
        
        // Create bookings for all users with diverse data
        for (let userIndex = 0; userIndex < userIds.length; userIndex++) {
            const userId = userIds[userIndex];
            
            // Each user gets 3-7 bookings for testing
            const bookingCount = 3 + (userIndex % 5);
            
            for (let i = 0; i < bookingCount; i++) {
                // Random status distribution
                const status = bookingStatuses[i % bookingStatuses.length];
                
                const today = new Date();
                const daysAgo = i * 7; // Spread bookings over time
                const dateFrom = new Date(today.getFullYear(), today.getMonth(), today.getDate() - daysAgo).toISOString().slice(0, 10);
                const dateTo = new Date(today.getFullYear(), today.getMonth(), today.getDate() - daysAgo + (i % 3) + 1).toISOString().slice(0, 10);
                const roomId = roomIds[i % roomIds.length];
                const hotelId = hotelIds[i % hotelIds.length];
                
                // Get room price for calculation
                const roomDoc = await db.collection('rooms').doc(roomId).get();
                const roomPrice = roomDoc.exists ? roomDoc.data().price : 150 + (i * 20);
                
                const nights = Math.max(1, (new Date(dateTo) - new Date(dateFrom)) / (1000 * 60 * 60 * 24));
                const basePrice = roomPrice * nights;
                const serviceFee = basePrice * 0.05;
                const taxes = basePrice * 0.1;
                const discount = i % 3 === 0 ? basePrice * 0.1 : 0;
                const totalPrice = basePrice + serviceFee + taxes - discount;
                
                const bookingRef = await db.collection('bookings').add({
                    userId: userId,
                    hotelId: hotelId,
                    roomId: roomId,
                    dateFrom: dateFrom,
                    dateTo: dateTo,
                    guests: 1 + (i % 4),
                    adults: 1 + (i % 3),
                    children: i % 3 === 0 ? 1 : 0,
                    rooms: 1 + (i % 2),
                    price: basePrice,
                    originalPrice: basePrice,
                    discount: discount,
                    serviceFee: serviceFee,
                    taxes: taxes,
                    totalPrice: totalPrice,
                    status: status,
                    paymentMethod: paymentMethods[i % paymentMethods.length],
                    specialRequests: i % 2 === 0 ? `Special request ${i + 1}: Please provide extra towels` : '',
                    preferences: {
                        highFloor: i % 3 === 0,
                        quietRoom: i % 4 === 0,
                        extraPillows: i % 5 === 0,
                        airportShuttle: i % 6 === 0,
                        earlyCheckIn: i % 7 === 0,
                        lateCheckOut: i % 8 === 0
                    },
                    createdAt: admin.firestore.Timestamp.now(),
                    updatedAt: admin.firestore.Timestamp.now(),
                    appliedVouchers: i % 4 === 0 ? [voucherIds[i % voucherIds.length]] : []
                });

                // Create corresponding bill
                const billRef = await db.collection('bills').add({
                    userId: userId,
                    bookingId: bookingRef.id,
                    amount: totalPrice,
                    status: i % 3 === 0 ? 'PAID' : 'UNPAID',
                    createdAt: admin.firestore.Timestamp.now()
                });
                
                // Create corresponding payment
                await db.collection('payments').add({
                    userId: userId,
                    billId: billRef.id,
                    amount: i % 3 === 0 ? totalPrice : 0,
                    method: paymentMethods[i % paymentMethods.length],
                    status: i % 3 === 0 ? 'COMPLETED' : 'PENDING',
                    createdAt: admin.firestore.Timestamp.now()
                });
            }
        }
        console.log(`✅ Created diverse bookings for ${userIds.length} users (3-7 bookings each) with bills and payments`);
    } else {
        console.warn('⚠️ No auth users or rooms -> skipping booking/bill/payment seed');
    }

    // Seed notifications for all users
    if (userIds.length > 0) {
        const notificationTitles = [
            'Booking Confirmed', 'Payment Successful', 'Check-in Reminder', 'Special Offer',
            'Booking Cancelled', 'Review Request', 'VIP Upgrade', 'New Deal Available',
            'Booking Modified', 'Welcome to ChillStay', 'Exclusive Discount', 'Trip Reminder'
        ];
        
        const notificationMessages = [
            'Your booking has been confirmed successfully!',
            'Payment processed successfully for your booking.',
            'Don\'t forget to check in tomorrow!',
            'Special offer available for your next trip!',
            'Your booking has been cancelled as requested.',
            'Please rate your recent stay experience.',
            'Congratulations! You\'ve been upgraded to VIP status.',
            'New exclusive deals are now available!',
            'Your booking details have been updated.',
            'Welcome to ChillStay! Enjoy your stay.',
            'Exclusive discount code: SAVE20',
            'Your trip is coming up soon!'
        ];
        
        for (let userIndex = 0; userIndex < userIds.length; userIndex++) {
            const notificationCount = 5 + (userIndex % 8); // 5-12 notifications per user
            
            for (let i = 0; i < notificationCount; i++) {
                await db.collection('notifications').add({
                    userId: userIds[userIndex],
                    title: notificationTitles[i % notificationTitles.length],
                    message: notificationMessages[i % notificationMessages.length],
                    read: i % 3 === 0, // Some notifications are read
                    createdAt: admin.firestore.Timestamp.now()
                });
            }
        }
        console.log(`✅ Created notifications for ${userIds.length} users`);
    } else {
        console.warn('⚠️ No auth users -> skipping notifications seed');
    }
}

// Danger: wipe and re-seed essential collections for ChillStay
async function resetAndSeedChillStay() {
    const admin = require('firebase-admin');
    const db = admin.firestore();

    const collections = [
        'users','hotels','rooms','bookings','reviews','bookmarks','vouchers','bills','payments','notifications'
    ];

    for (const col of collections) {
        const snap = await db.collection(col).get();
        const batch = db.batch();
        snap.forEach(doc => batch.delete(doc.ref));
        await batch.commit();
        console.log(`🧹 Cleared ${col}: ${snap.size} docs`);
    }

    await seedChillStaySample();
    console.log('✅ Re-seeded ChillStay sample data');
}
