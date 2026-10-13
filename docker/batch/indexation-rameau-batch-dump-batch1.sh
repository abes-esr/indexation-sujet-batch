#!/bin/bash

exec java -jar /scripts/indexation-rameau-batch-batch1.jar \
          -Duser.timezone=Europe/Paris \
          -Duser.language=fr

