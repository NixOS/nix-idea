"use strict";

/**
 * @typedef {Object} ParserRun
 * @property {number} totalResets
 * @property {number} totalWeighedResets
 * @property {TokenStats[]} tokens
 * @property {Object.<string, FrameStats>} frames
 * @property {string} rootFrame
 */

/**
 * @typedef {Object} TokenStats
 * @property {number} index
 * @property {number} offset
 * @property {string} type
 * @property {number} totalPasses
 * @property {number} totalResets
 * @property {number} totalWeighedResets
 */

/**
 * @typedef {Object} FrameStats
 * @property {string} key
 * @property {number} startToken
 * @property {string[]} parents
 * @property {string[]} children
 * @property {number} occurrences
 * @property {number} totalResets
 * @property {number} totalWastedAdvances
 * @property {number} totalResetsFinished
 * @property {number} maxResets
 * @property {number} maxWastedAdvances
 * @property {number} maxResetsFinished
 * @property {number} lastSeenToken
 * @property {number} lastSeenLocalToken
 * @property {number} endTokenIndex
 */

/**
 * @typedef {Object} State
 * @property {string|number|null} selection
 * @property {string|number|null} hover
 * @property {"linear"|"logarithmic"} heatmapScale
 * @property {"passes"|"resets"|"weighedResets"|"wasted"} heatmapSource
 * @property {"occurrences"|"maxResets"|"totalResets"|"maxWastedAdvances"|"totalWastedAdvances"|"maxResetsFinished"|"totalResetsFinished"} frameSort
 */

/**
 * @callback WithStateCallback
 * @param {State} state
 */

/**
 * @param {HTMLDivElement} div
 */
function main(div) {
    /** @type {ParserRun} */
    const data = JSON.parse(div.querySelector(":scope > .data").text);
    const code = div.querySelector(":scope > pre.code-preview");

    console.debug("data loaded: ", data);

    const state = stateProxy(update, {
        selection: null,
        selectionRange: null,
        hover: null,
        heatmapScale: "linear",
        heatmapSource: "passes",
        frameSort: "totalWastedAdvances",
    });

    const codeHeaderSlot = new Slot();
    const sidePanelSlot = new Slot();
    code.after(codeHeaderSlot.anchor, sidePanelSlot.anchor);

    function update() {
        console.debug("update UI; state: ", state);
        updateCodePreview(code, data, state);

        let sidePanelContent;
        if (state.selection == null) {
            sidePanelContent = framePanel(null, data, state);
        } else if (typeof state.selection === "string") {
            sidePanelContent = framePanel(state.selection, data, state);
        } else if (typeof state.selection === "number") {
            sidePanelContent = tokenPanel(state.selection, data, state);
        }

        codeHeaderSlot.update(html`
            <div class="code-config">
                <form>
                    <fieldset>
                        <legend>Heatmap:</legend>
                        ${radioButton(state, "heatmapSource", "passes")}
                        ${radioButton(state, "heatmapSource", "resets")}
                        ${radioButton(state, "heatmapSource", "weighedResets")}
                        ${radioButton(state, "heatmapSource", "wasted")}
                    </fieldset>
                    <fieldset>
                        <legend>Heatmap Scale:</legend>
                        ${radioButton(state, "heatmapScale", "linear")}
                        ${radioButton(state, "heatmapScale", "logarithmic")}
                    </fieldset>
                </form>
            </div>
        `)

        sidePanelSlot.update(html`
            <div class="side-panel">
                ${sidePanelContent}
            </div>
        `);
    }

    update();
}

/**
 * @param {number} token
 * @param {ParserRun} data
 * @param {State} state
 */
function tokenPanel(token, data, state) {
    const startingAt = Object.fromEntries(Object.entries(data.frames).filter(frame => frame[1].startToken === token));
    const table = frameTable("Related Frames", {
        "Frames starting here": startingAt,
    }, state);
    return html`
        <div class="token-panel">
            ${table}
        </div>
    `;
}

/**
 * @param {?string} frame
 * @param {ParserRun} data
 * @param {State} state
 */
function framePanel(frame, data, state) {
    let table;
    if (frame == null) {
        table = frameTable("All Frames", {"": data.frames}, state);
    } else {
        const frameData = data.frames[frame];
        const parents = Object.fromEntries(frameData.parents.map(p => [p, data.frames[p]]));
        const children = Object.fromEntries(frameData.children.map(p => [p, data.frames[p]]));
        table = frameTable("Related Frames", {
            "This Frame": {[frame]: frameData},
            "Parent Frames": parents,
            "Child Frames": children,
        }, state);
    }
    return html`
        <div class="frame-panel">
            ${table}
        </div>
    `;
}

/**
 * @param {string} title
 * @param {Object.<string, Object.<string, FrameStats>>} groups
 * @param {State} state
 */
function frameTable(title, groups, state) {
    const th = (title, sort) => html`
        <th onclick="${() => state.frameSort = sort}" class="${state.frameSort === sort ? "sortable sort-active" : "sortable"}">${title}</th>
    `;
    return html`
        <table>
            <caption>${title}</caption>
            <thead>
            <tr>
                <th>Frame</th>
                ${th("Occurrences", "occurrences")}
                ${th("Resets (Max)", "maxResets")}
                ${th("Resets (Total)", "totalResets")}
                ${th("Wasted (Max)", "maxWastedAdvances")}
                ${th("Wasted (Total)", "totalWastedAdvances")}
                ${th("Resets Finished (Max)", "maxResetsFinished")}
                ${th("Resets Finished (Total)", "totalResetsFinished")}
            </tr>
            </thead>
            ${Object.fromEntries(
                    Object.entries(groups)
                            .map(([key, frames]) => [key, frameTableGroup(key, frames, state)])
            )}
        </table>
    `;
}

/**
 * @param {string} title
 * @param {Object.<string, FrameStats>} frames
 * @param {State} state
 */
function frameTableGroup(title, frames, state) {
    const sortProperty = state.frameSort;
    const rows = Object.entries(frames)
        .sort((a, b) => b[1][sortProperty] - a[1][sortProperty])
        .map(([key, value]) => [key, frameTableRow(value, state)]);

    let header = null;
    let emptyMarker = null;
    if (title) {
        header = html`
            <tr>
                <th colspan="8">${title}</th>
            </tr>`;
    }
    if (rows.length === 0) {
        emptyMarker = html`
            <tr class="empty-row">
                <td colspan="8">None</td>
            </tr>`;
    }

    return html`
        <tbody>
        ${header}
        ${emptyMarker}
        ${Object.fromEntries(rows)}
        </tbody>
    `;
}

/**
 * @param {FrameStats} frame
 * @param {State} state
 */
function frameTableRow(frame, state) {
    const click = () => state.selection = frame.key;
    const enter = () => state.hover = frame.key;
    const leave = () => state.hover = null;
    return html`
        <tr class="frame-row" onmouseenter="${enter}" onmouseleave="${leave}" onclick="${click}">
            <td>${frame.key}</td>
            <td>${frame.occurrences}</td>
            <td>${frame.maxResets}</td>
            <td>${frame.totalResets}</td>
            <td>${frame.maxWastedAdvances}</td>
            <td>${frame.totalWastedAdvances}</td>
            <td>${frame.maxResetsFinished}</td>
            <td>${frame.totalResetsFinished}</td>
        </tr>
    `;
}

function radioButton(state, prop, value) {
    return html`
        <label>
            <input
                    type="radio"
                    name="${prop}"
                    value="${value}"
                    checked="${state[prop] === value}"
                    onclick="${() => state[prop] = value}"
            />
            ${value}
        </label>
    `;
}

/**
 * @param {HTMLPreElement} pre
 * @param {ParserRun} data
 * @param {State} state
 */
function updateCodePreview(pre, data, state) {
    removeRangeMarker(pre.querySelector(":scope span.hover"));
    removeRangeMarker(pre.querySelector(":scope span.selection"));

    /** @type {HTMLSpanElement[]} */
    const tokenElements = Array.from(pre.querySelectorAll(":scope > span[data-token]"));
    const heatFunction = getHeatFunction(data, state);

    for (const [index, span] of tokenElements.entries()) {
        console.assert(index === parseInt(span.dataset.token), "index", index, "!= data-token", span.dataset.token);
        updateToken(span, data, index, state, heatFunction);
        if (!span.onclick) {
            span.onclick = () => state.selection = index;
        }
    }

    if (state.hover) {
        injectRangeMarker(state.hover, "hover", "darkblue");
    } else {
        injectRangeMarker(state.selection, "selection", "green");
    }

    /**
     * @param {HTMLSpanElement} span
     * @param {ParserRun} data
     * @param {number} index
     * @param {State} state
     * @param {function} heatFunction
     */
    function updateToken(span, data, index, state, heatFunction) {
        const token = data.tokens[index];
        const heat = heatFunction(token);

        span.style.setProperty("--heat", `${100 * heat}%`);
        span.style.setProperty("--front-marker", "transparent");
        span.style.setProperty("--back-marker", "transparent");
        span.title = `${token.type}; passes: ${token.totalPasses}, resets: ${token.totalResets} (${token.totalWeighedResets})`;
    }

    /**
     * @param {ParserRun} data
     * @param {State} state
     */
    function getHeatFunction(data, state) {
        let heatFunction;
        switch (state.heatmapSource) {
            case "passes":
                heatFunction = token => token.totalPasses;
                break;
            case "resets":
                heatFunction = token => token.totalResets;
                break;
            case "weighedResets":
                heatFunction = token => token.totalWeighedResets;
                break;
            case "wasted":
                const tokenToFrames = [];
                for (let frame of Object.values(data.frames)) {
                    tokenToFrames[frame.startToken] ||= [];
                    tokenToFrames[frame.startToken].push(frame);
                }
                heatFunction = token => {
                    const frames = tokenToFrames[token.index] || [];
                    let result = 0;
                    frames.forEach(frame => {
                        result += frame.totalWastedAdvances;
                    });
                    return result;
                };
                break;
        }

        const max = data.tokens.reduce((max, token) => Math.max(heatFunction(token), max), 1);
        switch (state.heatmapScale) {
            case "logarithmic":
                return token => Math.log(heatFunction(token)) / Math.log(max);
            case "linear":
                return token => heatFunction(token) / max;
        }
    }

    function injectRangeMarker(range, className, color) {
        if (!range) return;

        let startIndex;
        let endIndex;
        let startMarkers = [];
        let endMarkers = [];
        if (typeof range === "string") {
            const frame = data.frames[range];
            startIndex = frame.startToken;
            startMarkers = [frame.startToken];
            endIndex = frame.lastSeenLocalToken;
            endMarkers = [frame.endTokenIndex, frame.lastSeenToken];
        } else if (typeof range === "number") {
            startIndex = range;
            endIndex = range + 1;
        }

        const start = tokenElements[startIndex];
        const end = tokenElements[endIndex];

        for (let markerIndex of startMarkers) {
            const marker = tokenElements[markerIndex];
            marker?.style?.setProperty("--front-marker", color);
        }
        for (let markerIndex of endMarkers) {
            const marker = tokenElements[markerIndex];
            marker?.style?.setProperty("--front-marker", color);
        }

        console.assert(start != null, "unknown start of", className, ":", startIndex);
        if (!start) return;

        console.assert(end == null || start.parentElement === end.parentElement, "non-matching parents:", start, end);

        const marker = document.createElement("span");
        marker.className = className;
        start.before(marker);

        const nodes = [];
        for (let c = start; c != null && c !== end; c = c.nextSibling) {
            nodes.push(c);
        }
        for (let c of nodes) {
            marker.append(c);
        }
    }

    function removeRangeMarker(marker) {
        if (!marker) return;

        for (const child of Array.from(marker.childNodes)) {
            marker.before(child);
        }
        marker.remove();
    }
}

/**
 * @param {WithStateCallback} callback
 * @param {State} state
 * @return {State}
 */
function stateProxy(callback, state) {
    let callbackScheduled = false;
    const result = wrap(state);
    return result;

    function wrap(substate) {
        return new Proxy(substate, {
            get(target, property, receiver) {
                const value = Reflect.get(...arguments);
                return value != null && typeof value === 'object' ? wrap(value) : value;
            },

            set(target, property, value, receiver) {
                if (!callbackScheduled && target[property] !== value) {
                    callbackScheduled = true;
                    // Maybe use `queueMicrotask` instead?
                    window.requestAnimationFrame(() => {
                        callbackScheduled = false;
                        callback(result);
                    });
                }
                return Reflect.set(...arguments);
            },
        });
    }
}

class Slot {

    /** @type {ChildNode} */
    #anchor;
    /** @type {TemplateInstance} */
    #previous = this.#parse(null, null);

    constructor(anchor) {
        this.#anchor = anchor || document.createTextNode("");
    }

    /**
     * @return {ChildNode}
     */
    get anchor() {
        return this.#anchor;
    }

    /**
     * @return {ChildNode[]}
     */
    get nodes() {
        return [this.#anchor, ...this.#previous.content];
    }

    /**
     * @param {*} input
     */
    update(input) {
        const previous = this.#previous;
        const next = this.#previous = previous.update(input);
        if (!arrayEquals(next.content, previous.content)) {
            previous.content.forEach(node => node.remove());
            this.#anchor.after(...next.content);
        }

        function arrayEquals(array1, array2) {
            if (array1 === array2) {
                return true;
            }
            if (array1.length !== array2.length) {
                return false;
            }
            for (let i = 0; i < array1.length; i++) {
                if (array1[i] !== array2[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * @typedef {Object} TemplateInstance
     * @property {ChildNode[]} content
     * @property {function|undefined} update
     */

    /** @return {TemplateInstance} */
    #parse(input, parseMethod) {
        parseMethod = parseMethod || this.#getParseMethod(input);
        const result = parseMethod.call(this, input);
        return this.#wrapParseResult(result, parseMethod);
    }

    #wrapParseResult(result, parseMethod) {
        return {
            content: result.content,
            update: (updatedInput) => {
                const newParseMethod = this.#getParseMethod(updatedInput);
                if (result.update != null && parseMethod === newParseMethod) {
                    const updateResult = result.update(updatedInput);
                    return this.#wrapParseResult(updateResult, parseMethod);
                } else {
                    return this.#parse(updatedInput, newParseMethod);
                }
            },
        };
    }

    #getParseMethod(input) {
        if (input == null) {
            return this.#parseNone;
        } else if (typeof input === "object" && typeof input.template === "string" && input.placeholders != null && input.args != null) {
            return this.#parseTemplate;
        } else if (typeof input === "object") {
            return this.#parseArray;
        } else {
            return this.#parseText;
        }
    }

    /** @return {TemplateInstance} */
    #parseNone() {
        return {content: []};
    }

    /** @return {TemplateInstance} */
    #parseText(input) {
        return {content: [document.createTextNode(input)]};
    }

    /**
     * @param {Object} input
     * @return {TemplateInstance}
     */
    #parseArray(input) {
        let slots = {};
        return update(input);

        function update(input) {
            slots = Object.fromEntries(Object.entries(input).map(([key, value]) => {
                const slot = slots[key] || new Slot();
                slot.update(value);
                return [key, slot];
            }));
            const content = Object.values(slots).flatMap(slot => slot.nodes);
            return {content, update};
        }
    }

    /**
     * @param {Template} input
     * @return {TemplateInstance}
     */
    #parseTemplate(input) {
        const template = document.createElement("template");
        template.innerHTML = input.template;

        const updates = [];
        scan(template.content.childNodes);
        update(input.args);

        const result = {
            content: Array.from(template.content.childNodes),
            update: (newInput) => {
                if (newInput.template === input.template) {
                    update(newInput.args);
                    return result;
                } else {
                    return this.#parseTemplate(newInput);
                }
            },
        };
        return result;

        /** @param {*[]} args */
        function update(args) {
            for (const update of updates) {
                update(args);
            }
        }

        /** @param {NodeList} nodes */
        function scan(nodes) {
            for (const node of nodes) {
                switch (node.nodeType) {
                    case Node.ELEMENT_NODE:
                        scanAttributes(node);
                        scan(node.childNodes);
                        break;
                    case Node.COMMENT_NODE:
                        scanComment(node);
                        break;
                }
            }
        }

        /** @param {HTMLElement} node */
        function scanAttributes(node) {
            let foundAttributes = [];
            for (let attr of node.attributes) {
                const index = input.placeholders[attr.value];
                if (index == null) continue;

                const name = attr.name === "class" ? "className" : attr.name;
                foundAttributes.push(attr);
                updates.push(args => {
                    if (node[name] !== args[index]) {
                        node[name] = args[index];
                    }
                });
            }
            foundAttributes.forEach(node.removeAttributeNode.bind(node));
        }

        /** @param {Comment} node */
        function scanComment(node) {
            const index = input.placeholders[`<!--${node.data}-->`];
            if (index == null) return;

            const subslot = new Slot(node);
            updates.push(args => subslot.update(args[index]));
        }
    }
}

/**
 * @typedef {Object} Template
 * @property {string} template
 * @property {Object.<string, number>} placeholders
 * @property {Array} args
 */

/**
 * @param {string[]} strings
 * @param {...*} args
 * @return {Template}
 */
function html(strings, ...args) {
    const placeholders = {};
    const values = [];
    const templateArray = [strings[0]];
    for (const [index, value] of args.entries()) {
        const placeholder = `<!-- PLACEHOLDER ${index} -->`;
        placeholders[placeholder] = values.length;
        values.push(value);
        templateArray.push(placeholder, strings[index + 1]);
    }

    return {
        template: templateArray.join(""),
        placeholders: placeholders,
        args: values,
    };
}

for (const div of document.querySelectorAll(".parser-run")) {
    main(div);
}
