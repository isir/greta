#!/bin/bash
cd /app

# Set GUI-friendly environment
export GRETA_HOME=/app
export GRETA_DATA=/app/data
export JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=false"

# Try to start GUI with fallback for LanguageMenu issues
echo 'Starting Greta GUI with error handling...'
java -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8 \
     -Djava.awt.headless=false \
     -Dapple.awt.application.name="Greta Platform" \
     -jar greta-application-modular-1.0.0-SNAPSHOT.jar 2>&1 | tee /tmp/greta.log

# If GUI fails, show the error
if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo "GUI startup failed. Error log:"
    tail -20 /tmp/greta.log
    echo "Keeping container alive for debugging..."
    tail -f /dev/null
fi
