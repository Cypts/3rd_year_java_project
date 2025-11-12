#!/bin/bash

# Student Management System Deployment Script
# This script helps deploy the application to Apache Tomcat

# Configuration
TOMCAT_HOME="/opt/tomcat"  # Update this path according to your Tomcat installation
WAR_FILE="target/student-management-system-1.0-SNAPSHOT.war"
APP_NAME="student-management-system"
MYSQL_USER="root"
MYSQL_PASSWORD="password"  # Update with your MySQL password

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
    print_message $YELLOW "Checking prerequisites..."
    
    # Check Java
    if command_exists java; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        print_message $GREEN "✓ Java found: $JAVA_VERSION"
    else
        print_message $RED "✗ Java not found. Please install Java 11 or higher."
        exit 1
    fi
    
    # Check Maven
    if command_exists mvn; then
        MVN_VERSION=$(mvn -version | head -n1 | awk '{print $3}')
        print_message $GREEN "✓ Maven found: $MVN_VERSION"
    else
        print_message $RED "✗ Maven not found. Please install Maven."
        exit 1
    fi
    
    # Check MySQL
    if command_exists mysql; then
        MYSQL_VERSION=$(mysql --version)
        print_message $GREEN "✓ MySQL found: $MYSQL_VERSION"
    else
        print_message $RED "✗ MySQL not found. Please install MySQL."
        exit 1
    fi
    
    # Check Tomcat
    if [ -d "$TOMCAT_HOME" ]; then
        print_message $GREEN "✓ Tomcat found at: $TOMCAT_HOME"
    else
        print_message $RED "✗ Tomcat not found at: $TOMCAT_HOME"
        print_message $YELLOW "Please update TOMCAT_HOME variable in this script"
        exit 1
    fi
}

# Setup database
setup_database() {
    print_message $YELLOW "Setting up database..."
    
    # Create database and tables
    mysql -u $MYSQL_USER -p$MYSQL_PASSWORD < sql/database_schema.sql
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "✓ Database setup completed successfully"
    else
        print_message $RED "✗ Database setup failed"
        exit 1
    fi
}

# Build application
build_application() {
    print_message $YELLOW "Building application..."
    
    # Clean and compile
    mvn clean compile
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "✓ Compilation successful"
    else
        print_message $RED "✗ Compilation failed"
        exit 1
    fi
    
    # Package WAR file
    mvn package
    
    if [ $? -eq 0 ] && [ -f "$WAR_FILE" ]; then
        print_message $GREEN "✓ WAR file created successfully: $WAR_FILE"
    else
        print_message $RED "✗ WAR file creation failed"
        exit 1
    fi
}

# Deploy to Tomcat
deploy_to_tomcat() {
    print_message $YELLOW "Deploying to Tomcat..."
    
    # Stop Tomcat if running
    if pgrep -f "catalina" > /dev/null; then
        print_message $YELLOW "Stopping Tomcat..."
        $TOMCAT_HOME/bin/shutdown.sh
        sleep 5
    fi
    
    # Remove existing deployment
    if [ -d "$TOMCAT_HOME/webapps/$APP_NAME" ]; then
        print_message $YELLOW "Removing existing deployment..."
        rm -rf $TOMCAT_HOME/webapps/$APP_NAME
    fi
    
    if [ -f "$TOMCAT_HOME/webapps/$APP_NAME.war" ]; then
        print_message $YELLOW "Removing existing WAR file..."
        rm -f $TOMCAT_HOME/webapps/$APP_NAME.war
    fi
    
    # Copy new WAR file
    print_message $YELLOW "Copying WAR file to Tomcat..."
    cp $WAR_FILE $TOMCAT_HOME/webapps/$APP_NAME.war
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "✓ WAR file deployed successfully"
    else
        print_message $RED "✗ WAR file deployment failed"
        exit 1
    fi
    
    # Start Tomcat
    print_message $YELLOW "Starting Tomcat..."
    $TOMCAT_HOME/bin/startup.sh
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "✓ Tomcat started successfully"
        print_message $GREEN "Application will be available at: http://localhost:8080/$APP_NAME"
    else
        print_message $RED "✗ Tomcat startup failed"
        exit 1
    fi
}

# Check application status
check_status() {
    print_message $YELLOW "Checking application status..."
    
    sleep 10  # Wait for application to start
    
    # Check if application is accessible
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/$APP_NAME/login)
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        print_message $GREEN "✓ Application is running successfully"
        print_message $GREEN "Access URL: http://localhost:8080/$APP_NAME/login"
        print_message $YELLOW "Default admin credentials: Username: admin, Password: admin123"
    else
        print_message $RED "✗ Application is not responding (HTTP Status: $HTTP_STATUS)"
        print_message $YELLOW "Check Tomcat logs for errors: $TOMCAT_HOME/logs/catalina.out"
    fi
}

# Show usage information
show_usage() {
    echo "Usage: $0 [OPTION]"
    echo "Options:"
    echo "  full-deploy    Complete deployment (database + build + deploy)"
    echo "  build-only     Only build the application"
    echo "  deploy-only    Only deploy to Tomcat"
    echo "  status         Check application status"
    echo "  help           Show this help message"
}

# Main execution
main() {
    case "$1" in
        full-deploy)
            print_message $GREEN "Starting full deployment..."
            check_prerequisites
            setup_database
            build_application
            deploy_to_tomcat
            check_status
            print_message $GREEN "Deployment completed successfully!"
            ;;
        build-only)
            print_message $GREEN "Building application only..."
            check_prerequisites
            build_application
            ;;
        deploy-only)
            print_message $GREEN "Deploying to Tomcat only..."
            check_prerequisites
            deploy_to_tomcat
            check_status
            ;;
        status)
            check_status
            ;;
        help|--help|-h)
            show_usage
            ;;
        *)
            print_message $RED "Invalid option: $1"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"