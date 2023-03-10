#!/bin/bash

set

OWNER=${OWNER:-"Default Team"}
MAIN_BRANCH=$(git symbolic-ref --short refs/remotes/origin/HEAD | awk -F/ '{print $NF}')
CIRCLE_BRANCH=${CIRCLE_BRANCH:-"${MAIN_BRANCH}"}

set

if [[ "${CIRCLE_BRANCH}" = "${MAIN_BRANCH}" ]]; then
  # run full scan on main branch
  echo '## Running full scan on main branch ##'

else

  echo '## Running incremental scan on main branch ##'

fi

