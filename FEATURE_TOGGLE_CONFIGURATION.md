# Feature Toggle Configuration Guide

## Overview

The feature toggle system has been updated to use **application.yml** as the primary configuration source, with optional database fallback. This provides a cleaner, simpler way to manage feature flags.

## Configuration Priority

The system checks feature flags in this order:

1. **application.yml** (Primary) - Simple, clean, version-controlled
2. **Database** (Fallback) - Runtime changes, dynamic flags
3. **false** (Default) - If not found anywhere

## YAML Configuration

### Basic Setup

Edit `src/main/resources/application.yml`:

```yaml
feature:
  toggle:
    # Enable/disable DB fallback (if true, checks DB when not found in YAML)
    db-fallback-enabled: true
    
    # Feature flags defined in YAML (simpler and cleaner)
    flags:
      use-new-flow: false
      # Add more feature flags here:
      # new-feature: true
      # another-feature: false
```

### Configuration Options

#### `db-fallback-enabled`
- **Type**: `boolean`
- **Default**: `true`
- **Description**: If `true`, checks database when feature not found in YAML. If `false`, only checks YAML.

#### `flags`
- **Type**: `Map<String, Boolean>`
- **Description**: Map of feature names to their enabled/disabled state
- **Example**:
  ```yaml
  flags:
    use-new-flow: false
    new-feature: true
    experimental-feature: false
  ```

## Usage Examples

### Example 1: Simple YAML-Only Configuration

```yaml
feature:
  toggle:
    db-fallback-enabled: false  # Disable DB fallback
    flags:
      use-new-flow: false
```

**Benefits**:
- ✅ Simple and clean
- ✅ Version controlled
- ✅ No database dependency
- ✅ Easy to review in code

**Use Case**: Static flags, environment-specific configs

### Example 2: YAML with DB Fallback

```yaml
feature:
  toggle:
    db-fallback-enabled: true  # Enable DB fallback
    flags:
      use-new-flow: false  # Default value
```

**Benefits**:
- ✅ YAML for defaults
- ✅ DB for runtime changes
- ✅ Best of both worlds

**Use Case**: Default in YAML, override via API for testing

### Example 3: Environment-Specific Configuration

**application-dev.yml**:
```yaml
feature:
  toggle:
    flags:
      use-new-flow: true  # Enable in dev
```

**application-prod.yml**:
```yaml
feature:
  toggle:
    flags:
      use-new-flow: false  # Disable in prod
```

## API Usage

### Check Feature Status

```bash
# Checks YAML first, then DB if enabled
GET /api/feature-toggles/{featureName}/enabled
```

### Enable Feature (Saves to DB)

```bash
# Saves to database
# Note: If feature exists in YAML, YAML value takes precedence when reading
POST /api/feature-toggles/{featureName}/enable
```

### Disable Feature (Saves to DB)

```bash
POST /api/feature-toggles/{featureName}/disable
```

## How It Works

### Code Flow

```java
public boolean isFeatureEnabled(String featureName) {
    // Step 1: Check YAML first
    if (properties.hasFlag(featureName)) {
        return properties.getFlag(featureName);
    }
    
    // Step 2: Check database if fallback enabled
    if (properties.isDbFallbackEnabled()) {
        Optional<FeatureToggle> dbToggle = repository.findByFeatureName(featureName);
        if (dbToggle.isPresent()) {
            return dbToggle.get().isEnabled();
        }
    }
    
    // Step 3: Default to false
    return false;
}
```

### Example Scenarios

#### Scenario 1: Feature in YAML only
```yaml
flags:
  use-new-flow: true
```
- **Result**: Returns `true` (from YAML)
- **DB Check**: Skipped

#### Scenario 2: Feature in DB only (YAML not found)
```yaml
flags: {}  # Empty
```
- **DB**: `use-new-flow: true`
- **Result**: Returns `true` (from DB)
- **YAML Check**: Not found, falls back to DB

#### Scenario 3: Feature in both YAML and DB
```yaml
flags:
  use-new-flow: false  # YAML value
```
- **DB**: `use-new-flow: true`
- **Result**: Returns `false` (YAML takes precedence)
- **Note**: YAML always wins when present

#### Scenario 4: Feature not found anywhere
- **YAML**: Not found
- **DB**: Not found (or fallback disabled)
- **Result**: Returns `false` (default)

## Advantages

### ✅ Simplicity
- Clean YAML configuration
- No database setup needed for simple flags
- Easy to understand and maintain

### ✅ Version Control
- Feature flags tracked in Git
- Easy to review changes
- Clear history of flag changes

### ✅ Environment-Specific
- Different values per environment
- Easy to enable/disable per environment
- No code changes needed

### ✅ Flexibility
- YAML for static flags
- DB for runtime changes
- Best of both worlds

### ✅ Performance
- YAML loaded at startup
- No database query for YAML flags
- Faster feature flag checks

## When to Use YAML vs Database

### Use YAML For:
- ✅ Static feature flags
- ✅ Environment-specific configs
- ✅ Flags that change with deployments
- ✅ Flags that should be version controlled
- ✅ Simple on/off switches

### Use Database For:
- ✅ Runtime changes (without restart)
- ✅ A/B testing
- ✅ Gradual rollouts
- ✅ Dynamic feature flags
- ✅ Flags that change frequently

## Migration from Database-Only

If you're migrating from database-only configuration:

1. **Add YAML configuration**:
   ```yaml
   feature:
     toggle:
       db-fallback-enabled: true
       flags:
         use-new-flow: false
   ```

2. **Existing DB flags still work** (if `db-fallback-enabled: true`)

3. **Gradually move flags to YAML** as needed

4. **Disable DB fallback** when all flags are in YAML:
   ```yaml
   db-fallback-enabled: false
   ```

## Best Practices

1. **Document flags**: Add comments in YAML explaining what each flag does
2. **Use meaningful names**: `use-new-flow` not `flag1`
3. **Set defaults**: Always set default values in YAML
4. **Review regularly**: Remove unused flags
5. **Environment-specific**: Use profiles for different environments

## Example: Complete Configuration

```yaml
feature:
  toggle:
    # Enable DB fallback for runtime changes
    db-fallback-enabled: true
    
    # Feature flags
    flags:
      # Enable new award processing flow
      use-new-flow: false
      
      # Enable new UI features
      new-ui: true
      
      # Experimental feature (disabled by default)
      experimental-feature: false
```

## Troubleshooting

### Feature flag not working?

1. **Check YAML**: Is the flag defined correctly?
2. **Check DB fallback**: Is `db-fallback-enabled: true`?
3. **Check logs**: Look for debug messages about flag resolution
4. **Check API**: Use `/api/feature-toggles/{name}/enabled` to verify

### YAML value not taking effect?

- **Restart required**: YAML is loaded at startup
- **Check syntax**: Ensure YAML is valid
- **Check precedence**: YAML always wins over DB

### Database value not working?

- **Check fallback**: Ensure `db-fallback-enabled: true`
- **Check YAML**: If flag exists in YAML, YAML takes precedence
- **Check API**: Verify flag exists in database

## Summary

The new YAML-based configuration provides:
- ✅ **Simpler**: Clean YAML configuration
- ✅ **Cleaner**: Less code, easier to understand
- ✅ **Flexible**: YAML + DB options
- ✅ **Performant**: YAML loaded at startup
- ✅ **Version controlled**: Flags tracked in Git

Perfect for managing feature flags in a clean, maintainable way!
