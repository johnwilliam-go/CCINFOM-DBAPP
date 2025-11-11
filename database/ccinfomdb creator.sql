-- Drop tables in correct dependency order
DROP TABLE IF EXISTS MaintenanceTracker;
DROP TABLE IF EXISTS ActivityLog;
DROP TABLE IF EXISTS OrderEntries;
DROP TABLE IF EXISTS KitchenOrderTicket;
DROP TABLE IF EXISTS MenuItems;
DROP TABLE IF EXISTS Equipments;
DROP TABLE IF EXISTS KitchenStaff;
DROP TABLE IF EXISTS KitchenStations;

-- 1. Kitchen Stations
CREATE TABLE KitchenStations (
    StationID INT PRIMARY KEY,
    StationName VARCHAR(50) NOT NULL,
    StationDescription VARCHAR(255)
);

-- 2. Menu Items
CREATE TABLE MenuItems (
    ItemID INT PRIMARY KEY,
    StationID INT,
    ItemName VARCHAR(50) NOT NULL,
    PricePerItem DOUBLE NOT NULL,
    ItemDescription VARCHAR(100) NOT NULL,
    FOREIGN KEY (StationID) REFERENCES KitchenStations(StationID)
);

-- 3. Kitchen Staff
CREATE TABLE KitchenStaff (
    StaffID INT PRIMARY KEY,
    UserID VARCHAR(50),
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    Role VARCHAR(50),
    EmploymentStatus ENUM('Active', 'On Leave', 'Resigned') DEFAULT 'Active'
);

-- 4. Kitchen Order Ticket
CREATE TABLE KitchenOrderTicket (
    KotID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerName VARCHAR(100) NOT NULL,
    OrderNumber VARCHAR(50) NOT NULL,
    TableNumber INT,
    OrderType ENUM('Dine In', 'Takeout') NOT NULL,
    PaymentMethod VARCHAR(50),
    OrderTime TIME NOT NULL DEFAULT (CURRENT_TIME()),
    OrderDate DATE NOT NULL DEFAULT (CURRENT_DATE()),
    PreparationTime INT,
    ExpectedOrderCompleted TIME,
    ActualOrderCompleted TIME,
    OrderDetails TEXT
);

-- 5. Order Entries
CREATE TABLE OrderEntries (
    KOTItemID INT PRIMARY KEY AUTO_INCREMENT,
    KotID INT NOT NULL,
    ItemID INT NOT NULL,
    PreparedBy INT,
    Quantity INT,
    CookingNotes VARCHAR(100),
    OrderStatus ENUM('In The Kitchen', 'Completed') DEFAULT 'In The Kitchen',
    PreparationTime TIME,
    TimeCompleted TIME,
    FOREIGN KEY (KotID) REFERENCES KitchenOrderTicket(KotID),
    FOREIGN KEY (ItemID) REFERENCES MenuItems(ItemID),
    FOREIGN KEY (PreparedBy) REFERENCES KitchenStaff(StaffID)
);

-- 6. Activity Log
CREATE TABLE ActivityLog (
    UsageID INT PRIMARY KEY AUTO_INCREMENT,
    KotID INT NOT NULL,
    OrderEntryID INT NOT NULL,
    ItemID INT,
    Quantity INT NOT NULL,
    StationID INT,
    UsageTime DATETIME,
    FOREIGN KEY (KotID) REFERENCES KitchenOrderTicket(KotID),
    FOREIGN KEY (OrderEntryID) REFERENCES OrderEntries(KOTItemID),
    FOREIGN KEY (ItemID) REFERENCES MenuItems(ItemID),
    FOREIGN KEY (StationID) REFERENCES KitchenStations(StationID)
);

-- 7. Equipments
CREATE TABLE Equipments (
    EquipmentID INT PRIMARY KEY,
    EquipmentName VARCHAR(100) NOT NULL,
    Category VARCHAR(50),
    Brand VARCHAR(50),
    Description VARCHAR(255),
    SupplierName VARCHAR(100),
    ContactNumber VARCHAR(20),
    EmailAddress VARCHAR(100)
);

-- 8. Maintenance Tracker
CREATE TABLE MaintenanceTracker (
    ReportID INT PRIMARY KEY AUTO_INCREMENT,
    EquipmentID INT NOT NULL,
    IssueType VARCHAR(50),
    Priority ENUM('Low', 'Medium', 'High') DEFAULT 'Medium',
    ReportDate DATE NOT NULL DEFAULT (CURRENT_DATE()),
    Status ENUM('In Progress', 'Resolved') DEFAULT 'In Progress',
    MaintenanceDate DATE,
    MaintenanceCost DOUBLE,
    Description TEXT,
    ReportedBy INT,
    FOREIGN KEY (EquipmentID) REFERENCES Equipments(EquipmentID),
    FOREIGN KEY (ReportedBy) REFERENCES KitchenStaff(StaffID)
);

-- ✅ Trigger to randomize PreparedBy
DELIMITER //
CREATE TRIGGER random_preparedby
BEFORE INSERT ON OrderEntries
FOR EACH ROW
BEGIN
    DECLARE randomStaff INT;
    SELECT StaffID INTO randomStaff
    FROM KitchenStaff
    ORDER BY RAND()
    LIMIT 1;
    SET NEW.PreparedBy = randomStaff;
END;
//
DELIMITER ;
