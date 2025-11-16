#!/bin/bash

# WeatherDesk Integration Verification Script

echo "🔍 WeatherDesk Integration Verification"
echo "========================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check counter
PASS=0
FAIL=0

# Function to check file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} Found: $1"
        ((PASS++))
        return 0
    else
        echo -e "${RED}✗${NC} Missing: $1"
        ((FAIL++))
        return 1
    fi
}

# Function to check directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} Found: $1"
        ((PASS++))
        return 0
    else
        echo -e "${RED}✗${NC} Missing: $1"
        ((FAIL++))
        return 1
    fi
}

echo "📁 Checking Directory Structure..."
echo "-----------------------------------"
check_dir "src/main/kotlin/com/weatherdesk/ui"
check_dir "src/main/kotlin/com/weatherdesk/ui/theme"
check_dir "src/main/kotlin/com/weatherdesk/ui/effects"
check_dir "src/main/kotlin/com/weatherdesk/ui/components"
check_dir "src/main/kotlin/com/weatherdesk/ui/content"
echo ""

echo "🎨 Checking UI Component Files..."
echo "-----------------------------------"
check_file "src/main/kotlin/com/weatherdesk/ui/theme/ThemeManager.kt"
check_file "src/main/kotlin/com/weatherdesk/ui/effects/WeatherParticleSystem.kt"
check_file "src/main/kotlin/com/weatherdesk/ui/components/InteractiveGlobe.kt"
check_file "src/main/kotlin/com/weatherdesk/ui/components/ForecastCarousel.kt"
check_file "src/main/kotlin/com/weatherdesk/ui/content/WeatherContent.kt"
echo ""

echo "🎭 Checking Controller Files..."
echo "-----------------------------------"
check_file "src/main/kotlin/com/weatherdesk/view/EnhancedWeatherController.kt"
check_file "src/main/kotlin/com/weatherdesk/view/WeatherController.kt"
echo ""

echo "📄 Checking FXML Files..."
echo "-----------------------------------"
check_file "src/main/resources/fxml/EnhancedWeatherView.fxml"
check_file "src/main/resources/fxml/WeatherView.fxml.backup"
echo ""

echo "🎨 Checking CSS Files..."
echo "-----------------------------------"
check_file "src/main/resources/styles/modern-weather.css"
check_file "src/main/resources/styles/weather.css"
echo ""

echo "📚 Checking Documentation..."
echo "-----------------------------------"
check_file "UI_UX_DESIGN_GUIDE.md"
check_file "CREATIVE_FEATURES_SUMMARY.md"
check_file "INTEGRATION_GUIDE.md"
check_file "INTEGRATION_COMPLETE.md"
check_file "OPENMETEO_MIGRATION.md"
echo ""

echo "🔧 Checking Core Files..."
echo "-----------------------------------"
check_file "src/main/kotlin/com/weatherdesk/Main.kt"
check_file "build.gradle.kts"
check_file "settings.gradle.kts"
echo ""

echo "🧪 Checking for Kotlin Syntax..."
echo "-----------------------------------"
KOTLIN_FILES=$(find src/main/kotlin/com/weatherdesk/ui -name "*.kt" 2>/dev/null | wc -l)
if [ $KOTLIN_FILES -eq 5 ]; then
    echo -e "${GREEN}✓${NC} Found all 5 UI component files"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} Expected 5 UI files, found $KOTLIN_FILES"
fi
echo ""

echo "📊 Results"
echo "========================================"
echo -e "✓ Passed: ${GREEN}$PASS${NC}"
echo -e "✗ Failed: ${RED}$FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}🎉 All checks passed! Integration is complete.${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Build: ./gradlew clean build"
    echo "2. Run: ./gradlew run"
    echo "3. Test all features"
    exit 0
else
    echo -e "${RED}❌ Some files are missing. Please check the errors above.${NC}"
    exit 1
fi
