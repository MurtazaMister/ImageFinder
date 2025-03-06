/**
 * Toggles visibility of advanced options panel and rotates arrow icon
 */
function toggleOptions() {
    const options = document.getElementById('advanced-options');
    const arrow = document.getElementById('arrow');
    if (options.style.display === 'none' || options.style.display === '') {
        options.style.display = 'block';
        arrow.classList.add('rotated');
    } else {
        options.style.display = 'none';
        arrow.classList.remove('rotated');
    }
}

/**
 * Shows/hides recursive level input based on checkbox state
 */
function showGoDeep() {
    const input = document.getElementById('go-deep-input');
    const label = document.getElementById('go-deep-label');
    const warning = document.getElementById('level-warning');

    if(document.getElementById('recurse').checked) {
        label.style.display = 'inline';
        input.style.display = 'inline';
        checkRecursiveLevel();
    }
    else {
        label.style.display = 'none';
        input.style.display = 'none';
        warning.style.display = 'none';
    }
}

/**
 * Validates recursive level input and shows warning if level > 2
 */
function checkRecursiveLevel() {
    const input = document.getElementById('go-deep-input');
    const warning = document.getElementById('level-warning');
    
    if (input.value > 2) {
        warning.style.display = 'block';
    } else {
        warning.style.display = 'none';
    }
}

/**
 * Validates if input string is a valid URL
 * @param url URL string to validate
 */
function isValidURL(url) {
    try {
        new URL(url);
        return true;
    } catch(e) {
        return false;
    }
}

/**
 * Handles API response and calls callback with parsed data
 * @param xhr XMLHttpRequest object
 * @param callback Function to handle response data
 */
function apiCallBack(xhr, callback) {
    if (xhr.readyState == XMLHttpRequest.DONE) {
        if (xhr.status != 200) {
            let message = xhr.status + ":" + xhr.statusText + ":" + xhr.responseText;
            alert(message);
            throw 'API call returned bad code: ' + xhr.status;
        }
        let response = xhr.responseText ? JSON.parse(xhr.responseText) : null;
        if (callback) {
            callback(response);
        }
    }
}

/**
 * Resets form state and UI elements to initial state
 */
function enableForm() {
    const form = document.getElementById('search-form');
    const submitButton = form.querySelector('.search-button');
    form.style.display = 'block';
    submitButton.disabled = false;
    document.getElementById('current-url-title').style.display = 'none';
    document.getElementById('grab-more-button').style.display = 'none';
    document.getElementById('loading-container').style.display = 'none';
}

/**
 * Makes API call to fetch images from URL
 * Handles streaming responses, timeouts, and error states
 * @param url API endpoint URL
 * @param method HTTP method (GET/POST)
 * @param obj Data to send with request
 * @param callback Function to handle response
 */
function makeApiCall(url, method, obj, callback) {
    let xhr = new XMLHttpRequest();
    let responseMap = {};
    let timeoutId;
    let accumulatedResponse = '';
    
    // Special category sets to avoid duplicates
    let logoSet = new Set();
    let gifSet = new Set();
    let faviconSet = new Set();
    
    function resetTimeout() {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            alert('Request timed out after 17.5 seconds of inactivity. The server took too long to respond.');
            enableForm();
        }, 17500);
    }
    
    function processSpecialImages(data) {
        // Create special category containers if they don't exist
        if (!responseMap['logos']) {
            responseMap['logos'] = {
                level: 'Logos',
                images: {},
                url: 'Logos'
            };
        }
        if (!responseMap['gifs']) {
            responseMap['gifs'] = {
                level: 'GIFs',
                images: {},
                url: 'GIFs'
            };
        }
        if (!responseMap['favicons']) {
            responseMap['favicons'] = {
                level: 'Favicons',
                images: {},
                url: 'Favicons'
            };
        }

        // Process each URL's images
        Object.entries(data).forEach(([url, urlData]) => {
            if (urlData.images) {
                Object.entries(urlData.images).forEach(([key, imgData]) => {
                    const imageUrl = imgData.imageUrl;
                    
                    // Process based on type
                    if (imgData.type === 'LOGO') {
                        if (!logoSet.has(imageUrl)) {
                            logoSet.add(imageUrl);
                            responseMap['logos'].images[imageUrl] = imgData;
                        }
                        delete urlData.images[key];
                    } else if (imgData.type === 'GIF') {
                        if (!gifSet.has(imageUrl)) {
                            gifSet.add(imageUrl);
                            responseMap['gifs'].images[imageUrl] = imgData;
                        }
                        delete urlData.images[key];
                    } else if (imgData.type === 'FAVICON') {
                        if (!faviconSet.has(imageUrl)) {
                            faviconSet.add(imageUrl);
                            responseMap['favicons'].images[imageUrl] = imgData;
                        }
                        delete urlData.images[key];
                    }
                });
                
                // Only keep the URL data if it has remaining images
                if (Object.keys(urlData.images).length > 0) {
                    responseMap[url] = urlData;
                }
            }
        });
    }
    
    document.getElementById('loading-container').style.display = 'block';
    resetTimeout();
    
    xhr.open(method, url);
    
    xhr.onerror = function() {
        clearTimeout(timeoutId);
        alert('Network error occurred. Please check your connection and try again.');
        enableForm();
    };
    
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.LOADING) {
            try {
                const newData = xhr.responseText.substring(accumulatedResponse.length);
                accumulatedResponse = xhr.responseText;

                if (newData.trim()) {
                    const chunks = newData.split('\n');
                    chunks.forEach(chunk => {
                        if (chunk.trim()) {
                            try {
                                const data = JSON.parse(chunk);
                                processSpecialImages(data);
                                updateList(responseMap);
                                resetTimeout();
                            } catch (e) {
                                // Skip incomplete chunks
                            }
                        }
                    });
                }
            } catch (error) {
                console.error('Error processing response:', error);
                enableForm();
            }
        } else if (xhr.readyState === XMLHttpRequest.DONE) {
            clearTimeout(timeoutId);
            document.getElementById('loading-container').style.display = 'none';
            
            if (xhr.status !== 200) {
                let message = xhr.status + ":" + xhr.statusText + ":" + xhr.responseText;
                alert(message);
                enableForm();
                throw 'API call returned bad code: ' + xhr.status;
            }
            if (Object.keys(responseMap).length > 0) {
                document.getElementById('grab-more-button').style.display = 'block';
            } else {
                alert('No images were found or the connection was interrupted.');
                enableForm();
            }
        }
    };
    
    const submitButton = document.querySelector('.search-button');
    submitButton.disabled = true;
    
    xhr.send(obj ? obj instanceof FormData || obj.constructor == String ? obj : JSON.stringify(obj) : null);
    
    window.responseMap = responseMap;
}

let currentZoom = 1;
const modal = document.getElementById('image-modal');
const modalImage = document.getElementById('modal-image');
const modalLink = document.getElementById('modal-link');
const modalClose = document.querySelector('.modal-close');

/**
 * Increases zoom level of modal image
 */
function zoomIn() {
    currentZoom = Math.min(currentZoom + 0.2, 3);
    modalImage.style.transform = `scale(${currentZoom})`;
}

/**
 * Decreases zoom level of modal image
 */
function zoomOut() {
    currentZoom = Math.max(currentZoom - 0.2, 0.5);
    modalImage.style.transform = `scale(${currentZoom})`;
}

/**
 * Resets zoom level of modal image to default
 */
function resetZoom() {
    currentZoom = 1;
    modalImage.style.transform = `scale(${currentZoom})`;
}

let currentFrameSize = 220;

/**
 * Adjusts size of image frames and their contained images
 * @param action 'increase' or 'decrease' to change frame size
 */
function adjustFrameSize(action) {
    const frames = document.querySelectorAll('.image-frame');
    const sizeChange = 20;
    
    if (action === 'increase') {
        currentFrameSize = Math.min(currentFrameSize + sizeChange, 400);
    } else {
        currentFrameSize = Math.max(currentFrameSize - sizeChange, 120);
    }
    
    frames.forEach(frame => {
        frame.style.width = `${currentFrameSize}px`;
        frame.style.height = `${currentFrameSize}px`;
        
        const img = frame.querySelector('img');
        if (img) {
            img.style.maxWidth = `${currentFrameSize - 20}px`;
            img.style.maxHeight = `${currentFrameSize - 20}px`;
        }
    });
}

/**
 * Displays images in main content area
 * Creates image frames and handles click events
 * @param images Object containing image data and metadata
 */
function displayImages(images) {
    const imageContainer = document.getElementById('image-container');
    imageContainer.innerHTML = '';
    
    document.getElementById('search-form').style.display = 'none';
    document.getElementById('loading-container').style.display = 'none';
    document.getElementById('grab-more-button').style.display = 'none';
    
    const submitButton = document.querySelector('.search-button');
    submitButton.disabled = false;
    
    const grabMoreButton = document.getElementById('grab-more-button');
    const currentUrlTitle = document.getElementById('current-url-title');
    
    currentUrlTitle.querySelector('a').href = images.url;
    currentUrlTitle.querySelector('a').textContent = images.url || 'Images';
    currentUrlTitle.style.display = 'flex';
    
    currentUrlTitle.querySelector('.size-controls').style.display = 'flex';
    
    grabMoreButton.style.display = 'block';
    
    const imageList = Object.entries(images)
        .map(([_, imageData]) => imageData)
        .filter(imageData => imageData && imageData.imageUrl);
    
    imageList.forEach(imageData => {
        const frame = document.createElement('div');
        frame.className = 'image-frame';
        frame.style.width = `${currentFrameSize}px`;
        frame.style.height = `${currentFrameSize}px`;
        
        const img = document.createElement('img');
        img.style.maxWidth = `${currentFrameSize - 20}px`;
        img.style.maxHeight = `${currentFrameSize - 20}px`;
        img.src = imageData.imageUrl;
        img.alt = 'Image';
        
        img.addEventListener('click', () => {
            modalImage.src = imageData.imageUrl;
            modalLink.href = imageData.imageUrl;
            const imageName = imageData.imageUrl.split('/').pop().split('?')[0];
            document.getElementById('modal-image-name').textContent = imageName;
            modal.style.display = 'block';
            resetZoom();
        });
        
        frame.appendChild(img);
        imageContainer.appendChild(frame);
    });
}

/**
 * Closes modal and resets zoom when close button is clicked
 */
modalClose.addEventListener('click', () => {
    modal.style.display = 'none';
    resetZoom();
});

/**
 * Closes modal and resets zoom when clicking outside image
 */
modal.addEventListener('click', (e) => {
    if (e.target === modal) {
        modal.style.display = 'none';
        resetZoom();
    }
});

let isDragging = false;
let startX, startY, translateX = 0, translateY = 0;

/**
 * Enables image dragging in modal when zoomed in
 */
modalImage.addEventListener('mousedown', (e) => {
    if (currentZoom > 1) {
        isDragging = true;
        startX = e.clientX - translateX;
        startY = e.clientY - translateY;
    }
});

/**
 * Updates image position while dragging in modal
 */
document.addEventListener('mousemove', (e) => {
    if (isDragging) {
        translateX = e.clientX - startX;
        translateY = e.clientY - startY;
        modalImage.style.transform = `scale(${currentZoom}) translate(${translateX}px, ${translateY}px)`;
    }
});

/**
 * Disables image dragging when mouse is released
 */
document.addEventListener('mouseup', () => {
    isDragging = false;
});

/**
 * Resets form and UI when grab more button is clicked
 */
document.getElementById('grab-more-button').addEventListener('click', function() {
    document.getElementById('grab-more-button').style.display = 'none';
    document.getElementById('current-url-title').style.display = 'none';
    document.getElementById('loading-container').style.display = 'none';
    
    document.getElementById('search-form').style.display = 'block';
    
    document.getElementById('image-container').innerHTML = '';
    
    document.querySelector('.search-bar').value = '';
    document.getElementById('recurse').checked = false;
    document.getElementById('go-deep-input').value = '';
    document.getElementById('go-deep-label').style.display = 'none';
    document.getElementById('go-deep-input').style.display = 'none';
    
    document.getElementById('advanced-options').style.display = 'none';
    document.getElementById('arrow').classList.remove('rotated');
});

/**
 * Updates sidebar with categorized image results
 * Groups images by level and handles expand/collapse
 * @param response Response data containing categorized images
 */
function updateList(response) {
    const imageContainer = document.getElementById('image-container');
    imageContainer.innerHTML = '';
    
    const sidebar = document.querySelector('.sidebar');
    sidebar.innerHTML = '';
    
    const specialLevels = ['Logos', 'GIFs', 'Favicons'];
    const levels = {};
    
    // First process special categories
    specialLevels.forEach(level => {
        const key = level.toLowerCase();
        if (response[key] && response[key].images && Object.keys(response[key].images).length > 0) {
            levels[level] = [{
                url: level,
                images: response[key].images
            }];
        }
    });
    
    // Then process numbered levels
    for (const url in response) {
        if (!specialLevels.map(l => l.toLowerCase()).includes(url)) {
            if (!response[url].images || Object.keys(response[url].images).length === 0) {
                continue;
            }
            
            const level = response[url].level;
            if (!levels[level]) {
                levels[level] = [];
            }
            levels[level].push({
                url: url,
                images: response[url].images
            });
        }
    }
    
    // Sort and create sections - only include special levels that have images
    const activeSpecialLevels = specialLevels.filter(level => levels[level]);
    const allLevels = [...activeSpecialLevels, ...Object.keys(levels).filter(l => !specialLevels.includes(l)).sort((a, b) => a - b)];
    
    allLevels.forEach(level => {
        if (!levels[level]) return;
        
        const levelSection = document.createElement('div');
        levelSection.className = 'level-section';
        
        const levelTitle = document.createElement('h2');
        levelTitle.className = 'level-title';
        
        const levelText = document.createElement('span');
        levelText.textContent = specialLevels.includes(level) ? level : `Level ${level}`;
        levelTitle.appendChild(levelText);
        
        const count = document.createElement('span');
        count.className = 'count';
        const itemCount = levels[level].length;
        count.textContent = `(${itemCount} ${itemCount === 1 ? 'item' : 'items'})`;
        levelTitle.appendChild(count);
        
        const arrow = document.createElement('span');
        arrow.className = 'arrow';
        arrow.textContent = '▼';
        levelTitle.appendChild(arrow);
        
        levelTitle.addEventListener('click', () => {
            levelSection.classList.toggle('expanded');
            arrow.classList.toggle('rotated');
        });
        
        levelSection.appendChild(levelTitle);
        
        levels[level].forEach(category => {
            const categorySection = document.createElement('div');
            categorySection.className = 'category-section';
            categorySection.setAttribute('data-category', category.url);
            
            const categoryTitle = document.createElement('h3');
            categoryTitle.textContent = category.url;
            categorySection.appendChild(categoryTitle);
            
            // Add image count bubble
            const imageCount = document.createElement('span');
            imageCount.className = 'image-count';
            const numImages = Object.keys(category.images).length;
            imageCount.textContent = numImages;
            categorySection.appendChild(imageCount);
            
            categorySection.addEventListener('click', () => {
                document.querySelectorAll('.category-section').forEach(cat => {
                    cat.classList.remove('active');
                });
                categorySection.classList.add('active');
                
                const imagesWithUrl = {
                    ...category.images,
                    url: category.url
                };
                displayImages(imagesWithUrl);
            });
            
            levelSection.appendChild(categorySection);
        });
        
        sidebar.appendChild(levelSection);
    });

    const firstLevel = sidebar.querySelector('.level-section');
    if (firstLevel) {
        firstLevel.classList.add('expanded');
        const firstArrow = firstLevel.querySelector('.arrow');
        if (firstArrow) {
            firstArrow.classList.add('rotated');
        }
    }
}

document.getElementById('search-form').addEventListener('submit', function(event) {
    event.preventDefault();

    const urlInput = document.querySelector('.search-bar');
    const recursiveCheckbox = document.getElementById('recurse');
    const recursiveLevelsInput = document.getElementById('go-deep-input');

    const url = urlInput.value;
    const recursive = recursiveCheckbox.checked;
    const recursiveLevels = recursiveLevelsInput.value || '0';

    if (isValidURL(url)) {
        document.getElementById('search-form').style.display = 'none';
        document.getElementById('grab-more-button').style.display = 'none';
        document.getElementById('current-url-title').style.display = 'none';
        
        document.getElementById('loading-container').style.display = 'block';
        
        let urlWithParams = `/main?url=${encodeURIComponent(url)}&recursive=${recursive}&recursiveLevels=${recursiveLevels}`;
        makeApiCall(urlWithParams, 'POST', null, updateList);
    } else {
        alert('Please enter a valid URL');
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const levelSections = document.querySelectorAll('.level-section');
    levelSections.forEach(section => {
        const title = section.querySelector('.level-title');
        const arrow = title.querySelector('.arrow');
        const count = title.querySelector('.count');
        
        const itemCount = section.querySelectorAll('.category-section').length;
        count.textContent = `(${itemCount} ${itemCount === 1 ? 'item' : 'items'})`;
        
        title.addEventListener('click', function() {
            section.classList.toggle('expanded');
            arrow.classList.toggle('rotated');
        });
        
        if (section.classList.contains('expanded')) {
            arrow.classList.add('rotated');
        }
    });
});
