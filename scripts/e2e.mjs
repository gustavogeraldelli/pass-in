const apiBaseUrl = process.env.API_BASE_URL ?? "http://127.0.0.1:8080";
const frontendOrigin = process.env.FRONTEND_ORIGIN ?? "http://localhost:9090";
const alternateFrontendOrigin = process.env.ALTERNATE_FRONTEND_ORIGIN ?? "http://127.0.0.1:9090";

const runId = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
const organizer = {
    name: "E2E Organizer",
    email: `organizer-${runId}@example.com`,
    password: "password123"
};
const attendee = {
    name: "E2E Attendee",
    email: `attendee-${runId}@example.com`
};

function log(message) {
    console.log(`[e2e] ${message}`);
}

function fail(message) {
    throw new Error(message);
}

async function sleep(ms) {
    await new Promise((resolve) => setTimeout(resolve, ms));
}

async function parseResponse(response) {
    const text = await response.text();

    if (!text)
        return null;

    const contentType = response.headers.get("content-type") ?? "";

    if (contentType.includes("json"))
        return JSON.parse(text);

    return text;
}

async function request(path, options = {}) {
    const response = await fetch(`${apiBaseUrl}${path}`, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...(options.headers ?? {})
        }
    });
    const body = await parseResponse(response);

    return { response, body };
}

async function expectStatus(label, promise, expectedStatus) {
    const result = await promise;

    if (result.response.status !== expectedStatus)
        fail(`${label}: expected ${expectedStatus}, got ${result.response.status}: ${JSON.stringify(result.body)}`);

    log(`${label}: ${expectedStatus}`);
    return result;
}

function expectHeader(label, response, header, expectedValue) {
    const value = response.headers.get(header);

    if (value !== expectedValue)
        fail(`${label}: expected ${header}=${expectedValue}, got ${value}`);
}

function expectToken(label, value) {
    if (!value || typeof value !== "string")
        fail(`${label}: expected token string`);
}

function expectUtcDate(label, value) {
    if (!value || typeof value !== "string" || !value.endsWith("Z"))
        fail(`${label}: expected UTC ISO date ending with Z, got ${value}`);

    const parsed = Date.parse(value);

    if (Number.isNaN(parsed))
        fail(`${label}: expected parseable date, got ${value}`);

    if (parsed > Date.now() + 60_000)
        fail(`${label}: date is unexpectedly in the future, got ${value}`);
}

async function waitForApi() {
    for (let attempt = 1; attempt <= 30; attempt++) {
        try {
            const { response, body } = await request("/actuator/health");

            if (response.ok && body?.status === "UP") {
                log("API health: UP");
                return;
            }
        }
        catch {
            // The stack may still be starting.
        }

        await sleep(1_000);
    }

    fail(`API did not become healthy at ${apiBaseUrl}`);
}

async function testCors(origin) {
    const { response } = await expectStatus(
            `CORS preflight from ${origin}`,
            request("/auth/login", {
                method: "OPTIONS",
                headers: {
                    Origin: origin,
                    "Access-Control-Request-Method": "POST",
                    "Access-Control-Request-Headers": "content-type"
                }
            }),
            200);

    expectHeader(`CORS preflight from ${origin}`, response, "access-control-allow-origin", origin);
}

async function main() {
    await waitForApi();
    await testCors(frontendOrigin);
    await testCors(alternateFrontendOrigin);

    const invalidRegister = await expectStatus(
            "short password validation",
            request("/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    name: organizer.name,
                    email: organizer.email,
                    password: "short"
                })
            }),
            400);

    const shortPasswordMessage = JSON.stringify(invalidRegister.body);

    if (!shortPasswordMessage.includes("Password must have between 8 and 72 characters"))
        fail(`short password validation: missing explicit password message: ${shortPasswordMessage}`);

    await expectStatus(
            "invalid login",
            request("/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email: `missing-${runId}@example.com`,
                    password: "password123"
                })
            }),
            401);

    const register = await expectStatus(
            "organizer registration",
            request("/auth/register", {
                method: "POST",
                body: JSON.stringify(organizer)
            }),
            201);
    expectToken("access token", register.body?.accessToken);
    expectToken("refresh token", register.body?.refreshToken);

    const authorization = `${register.body.tokenType} ${register.body.accessToken}`;

    const createEvent = await expectStatus(
            "event creation",
            request("/events", {
                method: "POST",
                headers: { Authorization: authorization },
                body: JSON.stringify({
                    title: `E2E Event ${runId}`,
                    details: "Created by the app-level smoke test.",
                    maximumAttendees: 5
                })
            }),
            201);
    const eventId = createEvent.body;

    if (!eventId || typeof eventId !== "string")
        fail(`event creation: expected event id, got ${JSON.stringify(eventId)}`);

    const publicEvent = await expectStatus("public event details", request(`/events/${eventId}`), 200);

    if (publicEvent.body?.event?.id !== eventId)
        fail(`public event details: expected event id ${eventId}`);

    await expectStatus("protected event list without token", request("/events"), 401);

    const eventList = await expectStatus(
            "protected event list with token",
            request("/events", { headers: { Authorization: authorization } }),
            200);

    if (!eventList.body?.events?.some((event) => event.id === eventId))
        fail(`protected event list with token: created event ${eventId} was not found`);

    const registration = await expectStatus(
            "attendee registration",
            request(`/events/${eventId}/attendees`, {
                method: "POST",
                body: JSON.stringify(attendee)
            }),
            201);
    const attendeeId = registration.body;

    if (!attendeeId || typeof attendeeId !== "string")
        fail(`attendee registration: expected attendee id, got ${JSON.stringify(attendeeId)}`);

    const badge = await expectStatus("attendee badge", request(`/attendees/${attendeeId}/badge`), 200);
    expectToken("check-in token", badge.body?.checkInToken);

    await expectStatus("check-in", request(`/check-ins/${badge.body.checkInToken}`, { method: "POST" }), 204);
    await expectStatus("duplicate check-in", request(`/check-ins/${badge.body.checkInToken}`, { method: "POST" }), 409);

    const attendees = await expectStatus(
            "protected attendee list",
            request(`/events/${eventId}/attendees?page=0&size=10`, { headers: { Authorization: authorization } }),
            200);
    const attendeeItem = attendees.body?.attendees?.find((item) => item.id === attendeeId);

    if (!attendeeItem)
        fail(`protected attendee list: attendee ${attendeeId} was not found`);

    expectUtcDate("attendee createdAt", attendeeItem.createdAt);
    expectUtcDate("attendee checkInAt", attendeeItem.checkInAt);

    const csv = await expectStatus(
            "attendee CSV export",
            request(`/events/${eventId}/attendees/export`, { headers: { Authorization: authorization } }),
            200);

    if (!csv.body.includes(attendee.email) || !csv.body.includes("Z"))
        fail(`attendee CSV export: expected attendee email and UTC dates, got ${csv.body}`);

    log("E2E OK");
}

main().catch((error) => {
    console.error(`[e2e] ${error.message}`);
    process.exit(1);
});
