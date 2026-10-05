(function () {

    const CHECK_INTERVAL = 15000;

    function getToday() {
        const now = new Date();

        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, "0");
        const day = String(now.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    }


    function getCurrentMinutes() {

        const now = new Date();

        return (
            now.getHours() * 60 +
            now.getMinutes()
        );
    }


    function getReminderMinutes(time) {

        if (!time) {
            return null;
        }

        const parts =
            time.split(":");

        if (parts.length !== 2) {
            return null;
        }

        const hour =
            Number(parts[0]);

        const minute =
            Number(parts[1]);

        if (
            Number.isNaN(hour) ||
            Number.isNaN(minute)
        ) {
            return null;
        }

        return (
            hour * 60 +
            minute
        );
    }


    async function requestNotificationPermission() {

        if (!("Notification" in window)) {

            alert(
                "Is browser mein notifications supported nahi hain."
            );

            return;
        }

        if (Notification.permission === "granted") {

            showNotificationStatus(
                "🔔 Notifications already enabled."
            );

            return;
        }

        const permission =
            await Notification.requestPermission();

        if (permission === "granted") {

            showNotificationStatus(
                "✅ Notifications enabled!"
            );

            new Notification(
                "MomTask AI",
                {
                    body:
                        "Reminder notifications successfully enabled."
                }
            );

        } else {

            showNotificationStatus(
                "❌ Notification permission allow nahi hui."
            );
        }
    }


    function showNotificationStatus(text) {

        let status =
            document.getElementById(
                "notificationStatus"
            );

        if (!status) {

            status =
                document.createElement("div");

            status.id =
                "notificationStatus";

            status.style.marginTop =
                "10px";

            status.style.textAlign =
                "center";

            status.style.fontSize =
                "14px";

            status.style.color =
                "#666";

            const addButton =
                document.querySelector(
                    ".add-btn"
                );

            if (addButton) {

                addButton.parentNode
                    .appendChild(status);
            }
        }

        status.textContent = text;

        setTimeout(() => {

            if (status) {
                status.textContent = "";
            }

        }, 4000);
    }


    function createNotificationButton() {

        if (
            document.getElementById(
                "notificationButton"
            )
        ) {
            return;
        }

        const button =
            document.createElement("button");

        button.id =
            "notificationButton";

        button.textContent =
            "🔔 Enable Reminder Notifications";

        button.style.width =
            "100%";

        button.style.marginTop =
            "10px";

        button.style.padding =
            "11px";

        button.style.border =
            "1px solid #ddd";

        button.style.borderRadius =
            "10px";

        button.style.background =
            "#fff";

        button.style.cursor =
            "pointer";

        button.style.fontSize =
            "14px";

        button.onclick =
            requestNotificationPermission;

        const addButton =
            document.querySelector(
                ".add-btn"
            );

        if (addButton) {

            addButton.parentNode
                .appendChild(button);
        }
    }


    async function checkReminders() {

        if (
            !("Notification" in window)
        ) {
            return;
        }

        if (
            Notification.permission !==
            "granted"
        ) {
            return;
        }

        try {

            const response =
                await fetch(
                    "/api/tasks"
                );

            if (!response.ok) {
                return;
            }

            const tasks =
                await response.json();

            const today =
                getToday();

            const currentMinutes =
                getCurrentMinutes();


            tasks.forEach(task => {

                if (task.completed) {
                    return;
                }

                if (
                    task.reminderEnabled === false
                ) {
                    return;
                }

                if (
                    task.date !== today
                ) {
                    return;
                }

                if (!task.reminderTime) {
                    return;
                }

                const reminderMinutes =
                    getReminderMinutes(
                        task.reminderTime
                    );

                if (
                    reminderMinutes === null
                ) {
                    return;
                }

                if (
                    currentMinutes <
                    reminderMinutes
                ) {
                    return;
                }


                const notificationKey =
                    `momtask-reminder-${task.id}-${today}-${task.reminderTime}`;


                if (
                    localStorage.getItem(
                        notificationKey
                    )
                ) {
                    return;
                }


                new Notification(
                    "🔔 MomTask Reminder",
                    {
                        body:
                            task.task,

                        tag:
                            notificationKey,

                        requireInteraction:
                            true
                    }
                );


                localStorage.setItem(
                    notificationKey,
                    "true"
                );

            });

        } catch (error) {

            console.error(
                "Reminder check failed:",
                error
            );
        }
    }


    function startReminderEngine() {

        createNotificationButton();

        checkReminders();

        setInterval(
            checkReminders,
            CHECK_INTERVAL
        );
    }


    if (
        document.readyState ===
        "loading"
    ) {

        document.addEventListener(
            "DOMContentLoaded",
            startReminderEngine
        );

    } else {

        startReminderEngine();
    }

})();
