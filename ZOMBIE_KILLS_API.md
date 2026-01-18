# Zombie Kills API Documentation

## Endpoint
`POST /api/zombie-kills`

## Description
This API endpoint allows the Project Zomboid server to send real-time updates about player character stats, including zombie kills, location, health status, and more. The data is used to:
- Track individual character statistics
- Update player rankings
- Award currency points based on kills (1 point per kill)
- Maintain player profile across multiple characters

## Authentication
This endpoint does **not** require authentication (accessible publicly) to allow the game server to send updates directly.

## Request Body Format
```json
{
  "playerName": "John_Survivor",
  "playerId": "76561198012345678",
  "timestamp": "2024-01-15T14:30:00",
  "serverName": "Apocalipse [BR] - Main Server",
  "updateNumber": 42,
  "updateReason": "kill",
  "killsSinceLastUpdate": 5,
  "totalSessionKills": 150,
  "x": 10500.5,
  "y": 8750.2,
  "z": 0,
  "health": 85.5,
  "infected": false,
  "isDead": false,
  "inVehicle": true,
  "profession": "Firefighter",
  "hoursSurvived": 72.5
}
```

## Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `playerName` | String | Yes | In-game character name |
| `playerId` | String | Yes | Player's Steam ID (76561198...) |
| `timestamp` | String (ISO 8601) | Yes | When the update occurred |
| `serverName` | String | Yes | Name of the game server |
| `updateNumber` | Integer | No | Sequential update number |
| `updateReason` | String | No | Reason for update (e.g., "kill", "login", "death") |
| `killsSinceLastUpdate` | Integer | No | New kills since last update |
| `totalSessionKills` | Integer | No | Total kills in current session |
| `x` | Double | Yes | Character X coordinate |
| `y` | Double | Yes | Character Y coordinate |
| `z` | Integer | Yes | Character Z coordinate (floor level) |
| `health` | Double | Yes | Current health (0-100) |
| `infected` | Boolean | Yes | Whether character is infected |
| `isDead` | Boolean | Yes | Whether character is dead |
| `inVehicle` | Boolean | No | Whether character is in a vehicle |
| `profession` | String | No | Character's profession |
| `hoursSurvived` | Double | No | Hours survived in-game |

## Response Format

### Success Response (200 OK)
```json
{
  "success": true,
  "message": "Character stats updated successfully",
  "characterId": 123,
  "totalKills": 150,
  "currencyPoints": 150,
  "userTotalKills": 500,
  "userTotalPoints": 500
}
```

### Error Responses

**400 Bad Request** - Missing required fields
```json
{
  "success": false,
  "error": "Player ID (Steam ID) is required"
}
```

**404 Not Found** - User not registered
```json
{
  "success": false,
  "error": "User not found. Please login to the system first."
}
```

**500 Internal Server Error** - Server error
```json
{
  "success": false,
  "error": "Internal server error: [error details]"
}
```

## Business Logic

### Character Creation/Update
- If a character with the given `playerName` doesn't exist for the user, a new one is created
- If it exists, the stats are updated with the new data
- All updates are timestamped automatically

### Currency Points
- Players earn **1 currency point per zombie kill**
- Points are accumulated on the character
- User's total points = sum of all characters' points

### Kill Tracking
- `killsSinceLastUpdate` is added to the character's total
- Both character kills and user total kills are updated
- Rankings are recalculated based on zombie kills

### Character Status
- `isDead` flag marks dead characters
- Dead characters are excluded from active player rankings
- Character death doesn't delete the character record

## Integration Flow

1. **Game Server Setup**: Configure the Project Zomboid server mod to send POST requests to this endpoint
2. **Player Registration**: Players must login via Steam OAuth at least once to create their account
3. **Automatic Updates**: Server sends updates on:
   - Zombie kills
   - Player death
   - Periodic location/status updates
4. **Data Aggregation**: System automatically:
   - Creates/updates character records
   - Calculates totals for users
   - Updates rankings
   - Awards currency points

## Example cURL Request
```bash
curl -X POST https://your-domain.com/api/zombie-kills \
  -H "Content-Type: application/json" \
  -d '{
    "playerName": "John_Survivor",
    "playerId": "76561198012345678",
    "timestamp": "2024-01-15T14:30:00",
    "serverName": "Apocalipse [BR] - Main",
    "killsSinceLastUpdate": 5,
    "totalSessionKills": 150,
    "x": 10500.5,
    "y": 8750.2,
    "z": 0,
    "health": 85.5,
    "infected": false,
    "isDead": false,
    "profession": "Firefighter",
    "hoursSurvived": 72.5
  }'
```

## Database Schema

### Character Entity
- **id**: Primary key
- **user**: Many-to-One relationship with User
- **playerName**: Character name
- **serverName**: Game server
- **profession**: Character profession
- **zombieKills**: Total kills
- **currencyPoints**: Earned points
- **hoursSurvived**: Game time survived
- **isDead**: Death status
- **lastX, lastY, lastZ**: Last known coordinates
- **lastHealth**: Last health value
- **lastInfected**: Last infection status
- **lastInVehicle**: Last vehicle status
- **createdAt**: Character creation timestamp
- **lastUpdate**: Last update timestamp

### User-Character Relationship
- One User can have multiple Characters (One-to-Many)
- Characters track individual game stats
- User aggregates totals from all characters
- User methods: `getTotalZombieKills()`, `getTotalCurrencyPoints()`, `getActiveCharactersCount()`

## Viewing Rankings
Players can view rankings at: `/player`
- Top players ranking table (by zombie kills)
- User's own characters list with individual stats
- User's total kills and points across all characters
