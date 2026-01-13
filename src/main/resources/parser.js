const table = $(".wikitable")[0];
const rows = Array.from(table.querySelectorAll("tr"));

let categories = [];
let currentCategory = null;

rows.forEach(tr => {
    const ths = tr.querySelectorAll("th");
    const tds = tr.querySelectorAll("td");

    if (ths.length === 1 && ths[0].getAttribute("colspan") === "4") {
        const span = ths[0].querySelector(".mw-headline");
        if (span) {
            currentCategory = {
                category: span.textContent.trim(),
                items: []
            };
            categories.push(currentCategory);
        }
        return;
    }

    if (ths.length === 4) {
        return;
    }

    if (tds.length === 4 && currentCategory) {
        const iconImg = tds[0].querySelector("img");
        const nameLink = tds[1].querySelector("a");
        const pageLink = tds[2].querySelector("a");

        currentCategory.items.push({
            icon: iconImg ? iconImg.getAttribute("src") : null,
            name: nameLink ? nameLink.textContent.trim() : null,
            page: pageLink ? pageLink.getAttribute("href") : null,
            itemId: tds[3].textContent.trim()
        });
    }
});

const jsonString = JSON.stringify(categories, null, 2);
jsonString
