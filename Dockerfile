# Debian-based image: Chromium is packaged for both amd64 and arm64.
FROM maven:3.9.16-amazoncorretto-17-debian-trixie

RUN apt-get update \
    && apt-get install -y --no-install-recommends chromium chromium-driver \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --uid 1000 tester \
    && mkdir /app \
    && chown tester:tester /app
USER tester
WORKDIR /app

# Dependencies are cached in their own layer and re-resolved only when pom.xml changes.
COPY --chown=tester:tester pom.xml .
RUN mvn -B -q dependency:go-offline

# Compiles the tests and resolves the Surefire provider without running any test.
COPY --chown=tester:tester src ./src
RUN mvn -B -q test -Dgroups=none

# Offline run: everything was resolved at build time.
# --no-sandbox: Chromium's sandbox cannot start under Docker's default security profile.
ENTRYPOINT ["mvn", "-B", "-o", "test", \
            "-Dselenide.browserBinary=/usr/bin/chromium", \
            "-Dwebdriver.chrome.driver=/usr/bin/chromedriver", \
            "-Dchromeoptions.args=--no-sandbox"]
CMD []
