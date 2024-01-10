import {Collapse} from "bootstrap";

function setupCollapsableComponents() {
    const collapseElementList = document.querySelectorAll(".collapse");
    [...collapseElementList].forEach(collapseEl =>
        collapseEl.addEventListener("click", () => new Collapse(collapseEl))
    );
}

window.addEventListener("load", setupCollapsableComponents);

console.error("Ooops! Ainda tem algumas coisas para customizar no template");