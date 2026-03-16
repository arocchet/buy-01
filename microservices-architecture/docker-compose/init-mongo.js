// Buy01 - MongoDB Initialization Script
// Creates databases and users for each microservice

const databases = [
    { name: 'userservice',    user: 'userservice',    password: 'userservice123' },
    { name: 'productservice', user: 'productservice', password: 'productservice123' },
    { name: 'mediaservice',   user: 'mediaservice',   password: 'mediaservice123' },
    { name: 'orderservice',   user: 'orderservice',   password: 'orderservice123' },
];

databases.forEach(({ name, user, password }) => {
    db = db.getSiblingDB(name);
    db.createUser({
        user: user,
        pwd:  password,
        roles: [{ role: 'readWrite', db: name }]
    });
    // Create a placeholder collection so the DB is visible
    db.createCollection('_init');
    print(`✅ Database '${name}' initialized.`);
});
