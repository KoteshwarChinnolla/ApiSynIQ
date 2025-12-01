let apiJson = null;
let endpoints = [];
fetchAndRender();
     // ---- EVENT HANDLERS ----
document.getElementById("connectBtn").onclick = connectToApi;
document.getElementById("runBtn").onclick = runQuery;


function showApiResolver(){
  const apiResolver = document.getElementById("apiResolver");
  const search = document.getElementById("search");
  const querySection = document.getElementById("querySection");
  apiResolver.style.display = apiResolver.style.display === 'none' ? 'block' : 'none';
  querySection.style.display = querySection.style.display === 'none' ? 'block' : 'none';
  // search.style.display = search.style.display === 'none' ? 'block' : 'none';
}

     // ---- FUNCTIONS ----
async function connectToApi() {
  const apiUrl = document.getElementById("apiUrl").value.trim();
  const statusEl = document.getElementById("status");
  const connectBox = document.getElementById("connectBox");
  const querySection = document.getElementById("querySection");
  const search = document.getElementById("search");

  if (!apiUrl) {
    alert("Please enter API base URL");
    return;
  }

  statusEl.textContent = "Connecting...";
  try {
    const res = await fetch(`${apiUrl}/SynIq/api/active`);
    const text = await res.text();
    if (text === "true") {
      statusEl.textContent = "✅ Connected to API Resolver";
      connectBox.style.display = "none";
      querySection.style.display = "flex";
      search.style.display = "none";
      window.syniqApiUrl = apiUrl;
    } else {
      statusEl.textContent = "❌ Connection failed (inactive)";
    }
  } catch (err) {
    console.error(err);
    statusEl.textContent = "❌ Failed to connect";
  }
}

async function runQuery() {
  const baseUrl = window.syniqApiUrl;
  if (!baseUrl) {
    alert("Please connect to API first!");
    return;
  }

  const query = document.getElementById("queryInput").value.trim();
  const limit = parseInt(document.getElementById("limitInput").value.trim());
    const endpointSelect = document.getElementById("endpointSelect");
  const useInput = endpointSelect.value === "searchMatchesForInputDescription";
  const useReturn = endpointSelect.value === "searchMatchesForReturnDescription";

  if (!query) {
    alert("Please enter a query string");
    return;
  }

  if (!useInput && !useReturn) {
    alert("Please select at least one endpoint");
    return;
  }

  const payload = { query, limit };
  console.log("📤 Payload:", payload);

  const promises = [];
  if (useInput) {
    promises.push(callEndpoint(`${baseUrl}/SynIq/api/searchMatchesForInputDescrition`, payload, "InputDescription"));
  }
  if (useReturn) {
    promises.push(callEndpoint(`${baseUrl}/SynIq/api/searchMatchesForReturnDescription`, payload, "ReturnDescription"));
  }

  await Promise.all(promises);
}

async function callEndpoint(url, payload, label) {
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    console.log(`✅ ${label} response:`, data);

    const endpointsArray = Array.isArray(data) ? data : [data];

    // Create a "controller" wrapper
    const controllers = [
      {
        id: `${label.toLowerCase()}-controller`,
        name: `${label} Results`,
        endpoints: endpointsArray
      }
    ];

    // Ensure DOM target exists
    let grid = document.getElementById("grid");
    if (!grid) {
      grid = document.createElement("div");
      grid.id = "grid";
      document.body.appendChild(grid);
    }

    // Also ensure count and empty placeholders exist
    if (!document.getElementById("count")) {
      const countDiv = document.createElement("div");
      countDiv.id = "count";
      document.body.appendChild(countDiv);
    }
    if (!document.getElementById("empty")) {
      const emptyDiv = document.createElement("div");
      emptyDiv.id = "empty";
      emptyDiv.style.display = "none";
      emptyDiv.textContent = "No endpoints found.";
      document.body.appendChild(emptyDiv);
    }

    // 🔥 Call your existing render function
    renderList(controllers);

  } catch (err) {
    console.error(`❌ ${label} error:`, err);
  }
}




async function fetchAndRender() {
  const baseUrl = window.location.origin;
  const url = `${baseUrl}/endpoints-info`;
  const grid = document.getElementById("grid");
  const empty = document.getElementById("empty");

  try {
    grid.innerHTML = "<div class='empty'>Loading...</div>";
    empty.style.display = "none";

    const response = await fetch(url, { cache: "no-store" });
    if (!response.ok) throw new Error("Failed to load API schema");

    apiJson = await response.json();
    endpoints = extractEndpoints(apiJson);
    console.log("Fetched:", apiJson);

    initializeFilters(apiJson, data => {
      const controllers = extractEndpoints(data);
      renderList(controllers);
      renderDropdownMenu(controllers);
    });


  } catch (err) {
    console.error(err);
    grid.innerHTML = `<div class="empty">Error loading endpoint data: ${err.message}</div>`;
  }
}

// flatten structure: the input is an array with a single object that has keys additionalProp1..3
// extractEndpoints: handle multiple controllers
function extractEndpoints(jsonObj) {
  const controllers = [];

  if (!jsonObj || typeof jsonObj !== "object") return controllers;

  // Each key is a controller name (e.g. "SampleController")
  Object.entries(jsonObj).forEach(([controllerName, endpointArray], index) => {
    const endpoints = [];

    // Each element inside endpointArray is an object with a single key (endpoint name)
    endpointArray.forEach(epObj => {
      Object.entries(epObj).forEach(([epKey, epVal]) => {
        endpoints.push({ key: epKey, ...epVal });
      });
    });

    controllers.push({
      id: `controller-${index + 1}`,
      name: controllerName,
      endpoints
    });
  });

  return controllers;
}


// renderList: display controllers separated by lines and titled
function renderList(controllers) {
  const grid = document.getElementById('grid');
  grid.innerHTML = '';
  let totalEndpoints = controllers.reduce((sum, c) => sum + c.endpoints.length, 0);
  // document.getElementById('count').textContent = `${totalEndpoints} endpoint(s)`;

  if (controllers.length === 0) {
    document.getElementById('empty').style.display = 'block';
    return;
  } else {
    document.getElementById('empty').style.display = 'none';
  }

  controllers.forEach((controller, index) => {
    // Controller Header
    const header = document.createElement('div');
    header.className = 'controller-header';
    header.id = controller.id;
    header.innerHTML = `
  <h2 class="controller-title">${escapeHtml(controller.name)}</h2>
  <hr class="controller-separator" />
`;
    grid.appendChild(header);

    // Render each endpoint under this controller
    controller.endpoints.forEach(ep => {
      const card = document.createElement('div');
      card.className = 'card';
      card.id = ep;

      const head = document.createElement('div');
      head.className = 'ep-head';
      head.id = ep.endpoint;

      const title = document.createElement('div');
      title.className = 'title-block';
      title.innerHTML = `
    <div class="ep-title">${escapeHtml(ep.name || ep.key)}</div>
    <div class="method-endpoint">
      <span class="httpMethod ${ep.httpMethod ? ep.httpMethod.toLowerCase() : ''}">
        ${escapeHtml(ep.httpMethod || 'N/A')}
      </span>
      <span class="ep-endpoint">${escapeHtml(ep.endpoint || '')}</span>
    </div>`;

      const tags = document.createElement('div');
      tags.className = 'tags';
      tags.innerHTML = (ep.tags || [])
        .map(t => `<span class="badge">${escapeHtml(t)}</span>`)
        .join(' ');

      head.appendChild(title);
      head.appendChild(tags);
      card.appendChild(head);

      if (ep.description) {
        const p = document.createElement('div');
        p.className = 'ep-description';
        p.textContent = ep.description;
        card.appendChild(p);
      }

      const d = document.createElement('div');
      d.className = 'details-box';

      const collapseAll = document.createElement('button');
      collapseAll.className = 'collapse-btn';
      collapseAll.innerHTML = `<span>⇲ Open All</span>`;

      const toggleBtn = document.createElement('button');
      toggleBtn.className = 'details-btn';
      toggleBtn.innerHTML = `<span>⇲ View Details</span>`;

      const tryBtn = document.createElement('button');
      tryBtn.className = 'try-btn';
      tryBtn.innerHTML = `<span>⇲ Try It Out</span>`;

      const content = document.createElement('div');
      content.className = 'details-content';
      content.style.display = 'none';

      const tryContent = document.createElement('div');
      tryContent.className = 'try-content';
      tryContent.style.display = 'none';

      // Toggle both
      collapseAll.onclick = () => {
        const open = content.style.display === 'none' || tryContent.style.display === 'none';
        content.style.display = open ? 'block' : 'none';
        tryContent.style.display = open ? 'block' : 'none';
        collapseAll.innerHTML = open ? `<span>⇱ Collapse All</span>` : `<span>⇲ Open All</span>`;
        toggleBtn.innerHTML = content.style.display === 'block' ? `<span>⇱ Hide Details</span>` : `<span>⇲ View Details</span>`;
        tryBtn.innerHTML = tryContent.style.display === 'block' ? `<span>⇱ Hide Try</span>` : `<span>⇲ Try It Out</span>`;
        if (tryContent.style.display === 'block' && !tryContent.dataset.loaded) {
          openApiTester(ep, tryContent);
          tryContent.dataset.loaded = 'true';
        }
      };

      toggleBtn.onclick = (e) => {
        e.stopPropagation();
        content.style.display = content.style.display === 'none' ? 'block' : 'none';
        toggleBtn.innerHTML = content.style.display === 'none' ? `<span>⇲ View Details</span>` : `<span>⇱ Hide Details</span>`;
      };

      tryBtn.onclick = (e) => {
        e.stopPropagation();
        tryContent.style.display = tryContent.style.display === 'none' ? 'block' : 'none';
        tryBtn.innerHTML = tryContent.style.display === 'none' ? `<span>⇲ Try It Out</span>` : `<span>⇱ Hide Try</span>`;
        if (tryContent.style.display === 'block' && !tryContent.dataset.loaded) {
          openApiTester(ep, tryContent);
          tryContent.dataset.loaded = 'true';
        }
      };

      d.appendChild(collapseAll);
      d.appendChild(toggleBtn);
      d.appendChild(tryBtn);
      d.appendChild(tryContent);
      d.appendChild(content);
      card.appendChild(d);

      // cons
      const describe = ep.inputsDescribe || {};
      // Populate details content
      if (describe.inputPathParams && Object.keys(describe.inputPathParams).length) {
        content.appendChild(
          makeSection("Path Parameters", makeParamsTable(describe.inputPathParams, ep.describeDtosForParms))
        );
      }

      // 🧩 QUERY / REQUEST PARAMETERS
      if (describe.inputQueryParams && Object.keys(describe.inputQueryParams).length) {
        content.appendChild(
          makeSection("Query / Req Parameters", makeParamsTable(describe.inputQueryParams, ep.describeDtosForParms))
        );
      }

      // 🧩 HEADERS
      if (describe.inputHeaders && Object.keys(describe.inputHeaders).length) {
        content.appendChild(
          makeSection("Request Headers", makeParamsTable(describe.inputHeaders, ep.describeDtosForParms))
        );
      }

      // 🧩 COOKIES
      if (describe.inputCookies && Object.keys(describe.inputCookies).length) {
        content.appendChild(
          makeSection("Request Cookies", makeParamsTable(describe.inputCookies))
        );
      }

      // 🧩 VARIABLES
      if (describe.inputVariables && Object.keys(describe.inputVariables).length) {
        content.appendChild(
          makeSection("Input Variables", makeParamsTable(describe.inputVariables, ep.describeDtosForParms))
        );
      }

      // 🧩 REQUEST BODY
      if (
        describe.inputBody &&
        describe.inputBody.requestBody &&
        Object.keys(describe.inputBody.requestBody).length
      ) {
        const rb = JSON.parse(describe.inputBody.requestBody);
        if (Array.isArray(rb)) {
          rb.forEach(rv => {
           const r = JSON.parse(rv)
            content.appendChild(
              makeSection(
                `Request Body — ${r.className || r.name || ""}`,
                makeFieldsTable(r.fields)
              )
            );
          });
        } else {
          // when requestBody is not array (like your Map<String, Object>)
          const bodyInfo = JSON.stringify(rb, null, 2);
          content.appendChild(
            makeSection("Request Body", createCodeBlock(bodyInfo))
          );
        }
      }

      // 🧩 RESPONSE BODY
      if (ep.responseBody) {
          const bodyInfo = JSON.stringify(ep.responseBody, null, 2);
          content.appendChild(
            makeSection("Request Body", createCodeBlock(bodyInfo))
          );
      }

      // 🧩 RETURN DESCRIPTION
      if (ep.returnDescription) {
        content.appendChild(
          makeSection("Return Description", createCodeBlock(ep.returnDescription))
        );
      }

      renderDtoSchemas(ep.dtoSchemas, content);

      grid.appendChild(card);
    });
  });
}
function renderDtoSchemas(dtoSchemas, container) {
  if (!dtoSchemas || !Object.keys(dtoSchemas).length) return;

  // Container for the whole DTO section
  const sectionWrapper = document.createElement('div');

  // Title + toggle button
  const header = document.createElement('div');
  header.style.display = 'flex';
  header.style.alignItems = 'center';
  header.style.justifyContent = 'space-between';
  header.style.marginBottom = '8px';

  const title = document.createElement('p');
  title.textContent = '▶ DTO Schemas';
  title.style.cursor = 'pointer';
  title.style.padding = '4px 8px 4px 16px';
  title.style.borderRadius = '4px';
  title.style.border = 'none';
  title.style.background = 'linear-gradient(to right, #aafa60ff, #aafa60ff)';
  title.style.color = '#002c39ff';
  header.appendChild(title);


  sectionWrapper.appendChild(header);

  // DTO Grid
  const dtoGrid = document.createElement('div');
  dtoGrid.className = 'dto-grid';
  dtoGrid.style.display = 'none'; // initially hidden
  dtoGrid.style.marginTop = '12px';

  // Loop over each DTO
  Object.entries(dtoSchemas).forEach(([dtoName, dto]) => {
    const dtoCard = document.createElement('div');
    dtoCard.className = 'dto-card';

    // DTO Header
    const dtoHeader = document.createElement('div');
    dtoHeader.className = 'dto-header';
    dtoHeader.innerHTML = `
      <h4>${escapeHtml(dtoName)}</h4>
      <p class="dto-class">${escapeHtml(dto.className || '')}</p>
      <p class="dto-desc">${escapeHtml(dto.description || 'No description')}</p>
    `;
    dtoCard.appendChild(dtoHeader);

    // DTO content
    if (Array.isArray(dto.fields) && dto.fields.length) {
      const bodyInfo = JSON.stringify(dto, null, 2);
      dtoCard.appendChild(makeSection("Dto", createCodeBlock(bodyInfo)));
    } else {
      const noFields = document.createElement('p');
      noFields.textContent = 'No fields available.';
      dtoCard.appendChild(noFields);
    }

    dtoGrid.appendChild(dtoCard);
  });

  sectionWrapper.appendChild(dtoGrid);
  container.appendChild(sectionWrapper);

  // Toggle behavior
  title.addEventListener('click', () => {
    const isHidden = dtoGrid.style.display === 'none';
    dtoGrid.style.display = isHidden ? 'flex' : 'none';
    title.textContent = isHidden ? '▼ DTO Schemas' : '▶ DTO Schemas';
  });
}


// 🔽 Inline “Try It Out” builder (no modal)
async function openApiTester(ep, container) {
  container.innerHTML = `
    <div class="tester-box">
      <h3>${ep.name}</h3>
      <p><strong>${ep.httpMethod}</strong> ${ep.endpoint}</p>
      <div class="input-sections"></div>
      <button class="send-btn">Send Request</button>
      <pre class="response-block"></pre>
    </div>
    `;

  const inputSections = container.querySelector('.input-sections');
  const responseBlock = container.querySelector('.response-block');

  if (ep.inputs) {
    buildInputForm(inputSections, ep.inputs.inputPathParams, 'Path Params');
    buildInputForm(inputSections, ep.inputs.inputQueryParams, 'Query Params');
    buildInputForm(inputSections, ep.inputs.inputVariables, 'Input Variables');
    buildInputJson(inputSections, ep.inputs.inputBody?.requestBody, 'Request Body');
    buildInputForm(inputSections, ep.inputs.inputHeaders, 'Headers');
  }

  const sendBtn = container.querySelector('.send-btn');
  sendBtn.onclick = async () => {
    responseBlock.textContent = 'Sending request...';
    const url = buildFullUrl(ep, inputSections);
    const method = ep.httpMethod || 'GET';

    // Collect all sections that could include files
    const bodyData = getFormData(inputSections, 'Request Body');
    const inputVars = getFormData(inputSections, 'Input Variables');
    const queryVars = getFormData(inputSections, 'Query Params');

    // Merge everything
    const allData = { ...inputVars, ...queryVars, ...bodyData };

    // Detect if any value is a File
    const hasFile = Object.values(allData).some(v => v instanceof File);

    // Headers
    const headers = getFormData(inputSections, 'Headers') || {};

    const config = {
      method,
      url,
      headers: { ...headers },
    };
    try {

      if (method !== 'GET') {
        if (hasFile) {
          // ✅ Use FormData for file upload
          const formData = new FormData();
          for (const [k, v] of Object.entries(allData)) {
            formData.append(k, v);
          }
          config.data = formData;
          // Axios will set Content-Type automatically for FormData
        } else {
          // Normal JSON/text
          if (
            typeof bodyData === 'string' ||
            typeof bodyData === 'number' ||
            typeof bodyData === 'boolean'
          ) {
            config.data = bodyData;
            config.headers['Content-Type'] = 'text/plain';
          } else if (Object.keys(bodyData).length > 0) {
            config.data = bodyData;
            config.headers['Content-Type'] = 'application/json';
          }
        }
      }

      const response = await axios(config);
      responseBlock.textContent = JSON.stringify(response.data, null, 2);
    } catch (err) {
      if (err.response)
        responseBlock.textContent = `Error ${err.response.status}: ${JSON.stringify(err.response.data, null, 2)}`;
      else
        responseBlock.textContent = 'Error: ' + err.message;
    }
  };
}

function buildInputJson(container, obj, title) {
  if (!obj) return;


  const section = document.createElement('div');
  section.className = 'input-section';
  section.innerHTML = `<h3>${title}</h3>`;

  // Create editable textarea instead of code block
  const textarea = document.createElement('textarea');
  textarea.className = 'json-input';
  textarea.value = JSON.stringify(JSON.parse(obj), null, 2);
  textarea.style.width = '100%';
  textarea.style.minHeight = '300px';
  textarea.style.fontFamily = 'monospace';
  textarea.style.fontSize = '14px';
  textarea.style.padding = '8px';
  textarea.style.border = '1px solid #ccc';
  textarea.style.borderRadius = '6px';
  textarea.style.resize = 'vertical';

  section.appendChild(textarea);
  container.appendChild(section);
}

// Helper to create inputs
function buildInputForm(container, obj, title) {
  if (!obj || Object.keys(obj).length === 0) return;

  const section = document.createElement('div');
  section.className = 'input-section';
  section.innerHTML = `<h3>${title}</h3>`;

  for (const [k, v] of Object.entries(obj)) {
    const div = document.createElement('div');
    div.className = 'input-row';

    // Convert value safely and trim extra quotes if any
    let cleanValue = v;
    if (typeof v === 'string') {
      cleanValue = v.trim();
      if (
        (cleanValue.startsWith('"') && cleanValue.endsWith('"')) ||
        (cleanValue.startsWith("'") && cleanValue.endsWith("'"))
      ) {
        cleanValue = cleanValue.slice(1, -1).trim();
      }
    }

    if (cleanValue === 'Upload File') {
      div.innerHTML = `
        <label>${k}</label>
        <input type="file" name="${k}" />
      `;
    } else {
      div.innerHTML = `
        <label>${k}</label>
        <input type="text" name="${k}" value="${cleanValue}" />
      `;
    }

    section.appendChild(div);
  }

  container.appendChild(section);
}

function getFormData(container, title) {
  const section = [...container.querySelectorAll('.input-section')]
    .find(s => s.querySelector('h3')?.textContent === title);
  if (!section) return {};

  if (title === 'Request Body') {
    const textarea = section.querySelector('.json-input');
    if (!textarea) return {};

    try {
      const parsed = JSON.parse(textarea.value);
      return parsed || {};
    } catch (err) {
      alert('Invalid JSON in Request Body');
      return {};
    }
  }

  const data = {};
  section.querySelectorAll('input').forEach(input => {
    if (!input.name) return;

    if (input.type === 'file' && input.files.length > 0) {
      data[input.name] = input.files[0]; // ✅ store File object
    } else {
      data[input.name] = input.value;
    }
  });
  return data;
}


function buildFullUrl(ep, container) {
  let url = ep.endpoint || '';

  // Path params
  const pathParams = getFormData(container, 'Path Params');
  for (const [k, v] of Object.entries(pathParams)) {
    url = url.replace(`{${k}}`, encodeURIComponent(v));
  }

  // Query params (skip file inputs)
  const queryParams = getFormData(container, 'Query Params');
  const inputVars = getFormData(container, 'Input Variables');
  const allQuery = { ...queryParams, ...inputVars };

  const queryString = new URLSearchParams(
    Object.fromEntries(
      Object.entries(allQuery).filter(([_, val]) => !(val instanceof File))
    )
  ).toString();

  if (queryString) url += (url.includes('?') ? '&' : '?') + queryString;

  return url;
}


function makeSection(title, node) {
  const wrapper = document.createElement('div');
  const h = document.createElement('strong');
  h.textContent = title;
  h.style.display = 'block';
  h.style.marginTop = '10px';
  wrapper.appendChild(h);
  wrapper.appendChild(node);
  return wrapper;
}

function makeParamsTable(map, listDescribeDtos) {
    console.log(map);
     console.log(listDescribeDtos);
   const table = document.createElement('table');
   const thead = document.createElement('thead');
   thead.innerHTML = `
     <tr>
       <th>Key</th>
       <th>Name</th>
       <th>Description</th>
       <th>Type</th>
       <th>Default</th>
       <th>AutoExec</th>
       <th>Options</th>
       <th>Example</th>
     </tr>`;
   table.appendChild(thead);

   const tbody = document.createElement('tbody');

   // Convert Map or object into iterable entries
   const entries = map instanceof Map ? map.entries() : Object.entries(map);

   for (const [key, value] of entries) {
     // Value might be a JSON string → parse it
     console.log(key)
     console.log(value)
     let parsedValue;
     try {
       parsedValue = listDescribeDtos[key];
     } catch (e) {
       console.warn(`Failed to parse value for key "${key}":`, e);
       parsedValue = {}; // fallback to empty object
     }

     const params = Array.isArray(parsedValue) ? parsedValue : [parsedValue];

     params.forEach(p => {
       const tr = document.createElement('tr');
       tr.innerHTML = `
         <td>${escapeHtml(key)}</td>
         <td>${escapeHtml(p.name || '')}</td>
         <td>${escapeHtml(p.description || '')}</td>
         <td>${escapeHtml(p.dataType || '')}</td>
         <td>${escapeHtml(p.defaultValue || '')}</td>
         <td>${escapeHtml(String(!!p.autoExecute))}</td>
         <td>${escapeHtml(p.options || '')}</td>
         <td>${escapeHtml(p.example || '')}</td>`;
       tbody.appendChild(tr);
     });
   }

   table.appendChild(tbody);
   return table;
 }

function makeFieldsTable(arr) {
  if (!Array.isArray(arr) || !arr.length) return createCodeBlock('-- no value provided --');
  const table = document.createElement('table');
  const thead = document.createElement('thead');
  thead.innerHTML = '<tr><th>Name</th><th>Description</th><th>Type</th><th>Default</th><th>AutoExec</th><th>Options</th><th>Example</th></tr>';
  table.appendChild(thead);
  const tbody = document.createElement('tbody');
  arr.forEach(f => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${escapeHtml(f.name || '')}</td>
                  <td>${escapeHtml(f.description || '')}</td>
                  <td>${escapeHtml(f.dataType || '')}</td>
                  <td>${escapeHtml(f.defaultValue || '')}</td>
                  <td>${escapeHtml(String(!!f.autoExecute))}</td>
                  <td>${escapeHtml(f.options || '')}</td>
                  <td>${escapeHtml(f.example || '')}</td>`;
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);
  return table;
}

function createCodeBlock(t) {
  const pre = document.createElement('pre');
  pre.textContent = typeof text === 'object' ? JSON.stringify(JSON.parse(t), null, 2) : t;
  return pre;
}

function escapeHtml(s) {
  if (s === undefined || s === null) return '';
  return String(s).replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

// search
document.getElementById('q').addEventListener('input', e => {
  const q = e.target.value.trim().toLowerCase();
  if (!q) return renderList(endpoints);

  const filtered = endpoints.flatMap(controller => {
    const matchingEndpoints = controller.endpoints.filter(ep => {
      // Helper to safely stringify any nested structure
      const deepString = (obj) => {
        if (obj == null) return '';
        if (typeof obj === 'string') return obj;
        if (Array.isArray(obj)) return obj.map(deepString).join(' ');
        if (typeof obj === 'object') {
          return Object.entries(obj)
            .map(([k, v]) => `${k} ${deepString(v)}`)
            .join(' ');
        }
        return String(obj);
      };

      // Combine *all* possible searchable fields:
      const combined = [
        ep.name,
        ep.endpoint,
        ep.httpMethod,
        ep.description,
        (ep.tags || []).join(' '),
        deepString(ep.pathParams),
        deepString(ep.reqParams),
        deepString(ep.requestBody),
        deepString(ep.responseBody),
        deepString(ep.inputs),
        deepString(ep.outputBody),
        (ep.filteringTags || []).join(' ')
      ].join(' ').toLowerCase();

      return combined.includes(q);
    });

    return matchingEndpoints.length ? [{ ...controller, endpoints: matchingEndpoints }] : [];
  });

  renderList(filtered);
});

// reset button (fixed: no duplicate listener)
document.getElementById('reset').addEventListener('click', () => {
  document.getElementById('q').value = '';
  fetchAndRender();
});

// download
document.getElementById('download').addEventListener('click', () => {
  if (!apiJson) return alert('No data loaded yet');
  const blob = new Blob([JSON.stringify(apiJson, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'api-endpoints.json';
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
});

function initializeFilters(jsonData, renderCallback) {
  const tagButtonsContainer = document.getElementById('tagButtons');

  // --- Collect all unique tags ---
  const allTags = new Set();
  Object.values(jsonData).forEach(controllerArray => {
    controllerArray.forEach(endpointObj => {
      const endpoint = Object.values(endpointObj)[0];
      if (endpoint.filteringTags) endpoint.filteringTags.forEach(tag => allTags.add(tag));
    });
  });

  // --- Tag Buttons ---
  tagButtonsContainer.innerHTML = '';
  allTags.forEach(tag => {
    const btn = document.createElement('button');
    btn.className = 'filter-btn';
    btn.textContent = tag;
    btn.onclick = () => {
      document.querySelectorAll('.tag-filters .filter-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const filtered = {};

      // Filter endpoints by tag
      Object.entries(jsonData).forEach(([controllerName, endpoints]) => {
        const filteredEndpoints = endpoints.filter(epObj => {
          const ep = Object.values(epObj)[0];
          return ep.filteringTags && ep.filteringTags.includes(tag);
        });
        if (filteredEndpoints.length > 0) filtered[controllerName] = filteredEndpoints;
      });

      renderCallback(filtered);
    };
    tagButtonsContainer.appendChild(btn);
  });

  // Default: show all
  renderCallback(jsonData);
}



function renderDropdownMenu(controllers) {
  const menuContainer = document.getElementById("menuContainer");
  menuContainer.innerHTML = ""; // Clear previous menus

  controllers.forEach((controller, index) => {
    const li = document.createElement("li");
    const a = document.createElement("a");
    a.href = "#";
    a.classList.add("menu");

    // Controller title
    const h2 = document.createElement("h2");
    h2.classList.add("menu-title");
    if (index === 1) h2.classList.add("menu-title_2nd");
    if (index === 2) h2.classList.add("menu-title_3rd");
    if (index === 3) h2.classList.add("menu-title_4th");
    h2.textContent = controller.name;

    // Dropdown list
    const ul = document.createElement("ul");
    ul.classList.add("menu-dropdown");

    controller.endpoints.forEach(ep => {
      const liInner = document.createElement("li");
      liInner.textContent = `${ep.httpMethod || ''} - ${ep.endpoint || ep.key}`;

      // Scroll to this controller section
      liInner.addEventListener("click", e => {
        e.preventDefault();
        const target = document.getElementById(ep.endpoint || ep.key);
        if (target) {
          target.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      });

      ul.appendChild(liInner);
    });

    a.appendChild(h2);
    a.appendChild(ul);
    li.appendChild(a);
    menuContainer.appendChild(li);
  });
}
