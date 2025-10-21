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

// Seed 10 hotels and rooms with new fields for ChillStay test
async function seedChillStaySample() {
    const admin = require('firebase-admin');
    const db = admin.firestore();

    const cities = [
        { city: 'Ho Chi Minh City', country: 'Vietnam' },
        { city: 'Hanoi', country: 'Vietnam' },
        { city: 'Da Nang', country: 'Vietnam' },
        { city: 'Bangkok', country: 'Thailand' },
        { city: 'Singapore', country: 'Singapore' }
    ];
    const hotelIds = [];
    const roomIds = [];

    for (let i = 0; i < 10; i++) {
        const loc = cities[i % cities.length];
        const hotel = {
            name: `Sample Hotel ${i + 1}`,
            country: loc.country,
            city: loc.city,
            rating: 4 + (i % 10) / 10,
            numberOfReviews: 20 + i * 3,
            imageUrl: 'https://placehold.co/600x400',
            minPrice: null,
            photoCount: 0
        };

        const hotelRef = await db.collection('hotels').add(hotel);
        hotelIds.push(hotelRef.id);

        const prices = [120 + i * 5, 160 + i * 5];
        let minPrice = null;
        const roomTypes = [
            { type: 'Standard', name: 'Standard Room', size: 20, view: 'City', capacity: 2, price: prices[0] },
            { type: 'Deluxe', name: 'Deluxe Room', size: 28, view: 'River', capacity: 3, price: prices[1] }
        ];
        
        for (let j = 0; j < roomTypes.length; j++) {
            const roomType = roomTypes[j];
            minPrice = minPrice == null ? roomType.price : Math.min(minPrice, roomType.price);
            const room = {
                hotelId: hotelRef.id,
                type: roomType.type,
                price: roomType.price,
                imageUrl: `https://placehold.co/500x300?text=${roomType.name.replace(' ', '+')}`,
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
    for (let i = 0; i < 2; i++) {
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

    // Seed bookmarks only if we have at least one auth user
    if (userIds.length > 0) {
        for (let i = 0; i < Math.min(5, hotelIds.length); i++) {
            await db.collection('bookmarks').add({
                userId: userIds[0],
                hotelId: hotelIds[i],
                createdAt: admin.firestore.Timestamp.now()
            });
        }
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

    // Seed vouchers
    const voucherIds = [];
    for (let i = 0; i < 10; i++) {
        const voucherRef = await db.collection('vouchers').add({
            code: `SAVE${10 + i}`,
            discount: 5 + (i % 5),
            type: 'PERCENT',
            expiresAt: new Date(Date.now() + 30 * 24 * 3600 * 1000)
        });
        voucherIds.push(voucherRef.id);
    }

    // Seed multiple bookings with diverse data for testing
    if (userIds.length > 0 && roomIds.length > 0) {
        const bookingStatuses = ['PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'];
        const paymentMethods = ['CREDIT_CARD', 'DEBIT_CARD', 'DIGITAL_WALLET', 'BANK_TRANSFER', 'CASH'];
        
        for (let i = 0; i < 15; i++) {
            const today = new Date();
            const dateFrom = new Date(today.getFullYear(), today.getMonth(), today.getDate() + (i * 2)).toISOString().slice(0, 10);
            const dateTo = new Date(today.getFullYear(), today.getMonth(), today.getDate() + (i * 2) + (i % 3) + 1).toISOString().slice(0, 10);
            const roomId = roomIds[i % roomIds.length];
            const userId = userIds[i % userIds.length];
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
                status: bookingStatuses[i % bookingStatuses.length],
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
                appliedVouchers: i % 4 === 0 ? [voucherIds[i % 5]] : []
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
        console.log('✅ Created 15 diverse bookings with bills and payments');
    } else {
        console.warn('⚠️ No auth users or rooms -> skipping booking/bill/payment seed');
    }

    // Seed notifications for first user
    if (userIds.length > 0) {
        for (let i = 0; i < 10; i++) {
            await db.collection('notifications').add({
                userId: userIds[0],
                title: `Notification ${i + 1}`,
                message: 'Your booking update',
                read: false,
                createdAt: admin.firestore.Timestamp.now()
            });
        }
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