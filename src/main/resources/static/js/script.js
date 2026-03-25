(function() {
    // Получаем ссылки на элементы DOM
    const canvas = document.getElementById('interferenceCanvas');
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    // Масштаб: пикселей на метр
    const PX_PER_METER = 30;

    // Функция обновления отображаемых значений слайдеров с одним знаком после запятой для длины волны
    window.updateValue = function(slider, displayId, unit) {
        const display = document.getElementById(displayId);
        if (displayId === 'wavelengthValue') {
            // Для длины волны - один знак после запятой
            display.textContent = parseFloat(slider.value).toFixed(1) + unit;
        } else if (displayId === 'slitWidthValue') {
            // Для ширины щели - два знака после запятой
            display.textContent = parseFloat(slider.value).toFixed(2) + unit;
        } else if (displayId === 'distanceValue') {
            // Для расстояния - один знак после запятой
            display.textContent = parseFloat(slider.value).toFixed(1) + unit;
        } else {
            display.textContent = slider.value + unit;
        }
        drawModel(); // перерисовываем canvas при изменении слайдера
    };

    // Функция для склонения слова "щель"
    function getSlitWord(n) {
        if (n % 10 === 1 && n % 100 !== 11) return 'щель';
        if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return 'щели';
        return 'щелей';
    }

    // Конвертер длины волны в RGB
    function wavelengthToRGB(lambda) {
        let r, g, b;
        if (lambda >= 380 && lambda < 440) {
            r = (440 - lambda) / (440 - 380);
            g = 0;
            b = 1;
        } else if (lambda >= 440 && lambda < 490) {
            r = 0;
            g = (lambda - 440) / (490 - 440);
            b = 1;
        } else if (lambda >= 490 && lambda < 510) {
            r = 0;
            g = 1;
            b = (510 - lambda) / (510 - 490);
        } else if (lambda >= 510 && lambda < 580) {
            r = (lambda - 510) / (580 - 510);
            g = 1;
            b = 0;
        } else if (lambda >= 580 && lambda < 645) {
            r = 1;
            g = (645 - lambda) / (645 - 580);
            b = 0;
        } else if (lambda >= 645 && lambda <= 780) {
            r = 1;
            g = 0;
            b = 0;
        } else {
            r = g = b = 1;
        }

        let factor;
        if (lambda >= 380 && lambda < 420) {
            factor = 0.3 + 0.7 * (lambda - 380) / (420 - 380);
        } else if (lambda >= 420 && lambda < 700) {
            factor = 1;
        } else if (lambda >= 700 && lambda <= 780) {
            factor = 0.3 + 0.7 * (780 - lambda) / (780 - 700);
        } else {
            factor = 0;
        }

        return {
            r: Math.floor(255 * r * factor),
            g: Math.floor(255 * g * factor),
            b: Math.floor(255 * b * factor)
        };
    }

    // Главная функция отрисовки
    function drawModel() {
        // Получаем текущие значения из слайдеров
        const wavelengthInput = document.getElementById('wavelength');
        const distanceInput = document.getElementById('distance');
        const slitWidthInput = document.getElementById('slitWidth');
        const slitNumberInput = document.getElementById('slitNumber');
        const sourceAngleInput = document.getElementById('sourceAngle');

        if (!wavelengthInput || !distanceInput || !slitWidthInput || !slitNumberInput || !sourceAngleInput) {
            return; // если элементы не найдены, выходим
        }

        // Считываем значения
        let lambda = parseFloat(wavelengthInput.value) || 550;
        let L = parseFloat(distanceInput.value) || 1.2;
        let d = parseFloat(slitWidthInput.value) || 0.3;
        let N = parseInt(slitNumberInput.value) || 2;
        let angleDeg = parseFloat(sourceAngleInput.value) || 90;

        // Физические величины в СИ
        const lambda_m = lambda * 1e-9;
        const d_m = d * 1e-3;
        const angleRad = angleDeg * Math.PI / 180;

        // Очистить canvas
        ctx.clearRect(0, 0, width, height);
        ctx.shadowColor = 'transparent';
        ctx.shadowBlur = 0;

        // Фиксированные координаты
        const screenX = 0.4 * width;
        const centerY = height / 2;
        const centerX = screenX;

        // Положение источника на дуге
        let sourceX = centerX - L * PX_PER_METER * Math.sin(angleRad);
        let sourceY = centerY + L * PX_PER_METER * Math.cos(angleRad);

        // Рисуем источник с мощным белым свечением
        ctx.shadowColor = '#ffffff';
        ctx.shadowBlur = 80;

        const gradient = ctx.createRadialGradient(sourceX, sourceY, 0, sourceX, sourceY, 40);
        gradient.addColorStop(0, '#ffffff');
        gradient.addColorStop(0.3, '#ffffff');
        gradient.addColorStop(0.6, 'rgba(255, 255, 255, 0.9)');
        gradient.addColorStop(0.8, 'rgba(255, 255, 255, 0.6)');
        gradient.addColorStop(1, 'rgba(255, 255, 240, 0.2)');

        ctx.beginPath();
        ctx.arc(sourceX, sourceY, 40, 0, 2 * Math.PI);
        ctx.fillStyle = gradient;
        ctx.fill();

        // Добавляем ореол
        ctx.shadowBlur = 100;
        ctx.beginPath();
        ctx.arc(sourceX, sourceY, 45, 0, 2 * Math.PI);
        ctx.fillStyle = 'rgba(255, 255, 255, 0.1)';
        ctx.fill();

        ctx.shadowBlur = 8;
        ctx.shadowColor = '#ffffff';

        // Подпись источника
        ctx.font = 'bold 14px "Segoe UI"';
        ctx.fillStyle = '#ffffff';
        ctx.fillText('ИСТОЧНИК', sourceX - 45, sourceY - 40);
        ctx.font = '12px "Segoe UI"';
        ctx.fillStyle = '#ffdcaa';
        ctx.fillText(`α = ${angleDeg}°`, sourceX - 35, sourceY - 60);

        // Линия от источника к центру экрана
        ctx.beginPath();
        ctx.moveTo(sourceX, sourceY);
        ctx.lineTo(centerX, centerY);
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 2;
        ctx.setLineDash([6, 4]);
        ctx.stroke();
        ctx.setLineDash([]);

        // Подпись расстояния
        ctx.fillStyle = '#ffffff';
        ctx.font = '12px "Segoe UI"';
        ctx.textAlign = 'center';
        ctx.fillText(`L = ${L.toFixed(1)} м`, (sourceX + centerX) / 2, (sourceY + centerY) / 2 - 10);
        ctx.textAlign = 'left';

        // Экран с щелями
        ctx.shadowBlur = 15;
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(screenX, 40);
        ctx.lineTo(screenX, height - 40);
        ctx.stroke();

        // Расчёт положений щелей
        const slitSpacing = 18;
        const startY = centerY - (N - 1) * slitSpacing / 2;
        const slitPositions = [];
        const slitFixedWidth = 6;
        const slitHeight = Math.max(4, Math.min(30, 2 + 22 * d));

        ctx.shadowBlur = 18;
        for (let i = 0; i < N; i++) {
            let yPos = startY + i * slitSpacing;
            if (yPos > 40 && yPos < height - 40) {
                slitPositions.push(yPos);
                ctx.fillStyle = '#000000';
                ctx.fillRect(screenX - slitFixedWidth / 2, yPos - slitHeight / 2, slitFixedWidth, slitHeight);
                ctx.strokeStyle = '#888888';
                ctx.lineWidth = 1;
                ctx.strokeRect(screenX - slitFixedWidth / 2, yPos - slitHeight / 2, slitFixedWidth, slitHeight);
            }
        }

        // Подписи экрана
        ctx.shadowBlur = 8;
        ctx.font = 'bold 14px "Segoe UI"';
        ctx.fillStyle = '#ffffff';
        ctx.fillText('ЭКРАН', screenX - 45, 25);
        ctx.font = '12px "Segoe UI"';
        ctx.fillStyle = '#ffffff';
        ctx.fillText(`${N} ${getSlitWord(N)}`, screenX + 5, height - 20);
        ctx.font = '10px monospace';
        ctx.fillStyle = '#cccccc';
        ctx.fillText(`h = ${slitHeight.toFixed(1)} px`, screenX + 10, height - 35);

        // Лучи от источника к щелям
        ctx.shadowBlur = 12;
        ctx.globalAlpha = 0.7;
        ctx.lineWidth = 1.5;
        ctx.strokeStyle = '#ffffff';
        for (let i = 0; i < slitPositions.length; i++) {
            ctx.beginPath();
            ctx.moveTo(sourceX, sourceY);
            ctx.lineTo(screenX, slitPositions[i]);
            ctx.stroke();
        }

        // Интерференционная картина
        const pictureX = 0.85 * width;
        const pictureHeight = 0.7 * height;
        const pictureTop = (height - pictureHeight) / 2;

        // Вспомогательные лучи
        ctx.lineWidth = 1.2;
        for (let i = 0; i < slitPositions.length; i++) {
            for (let k = -2; k <= 2; k++) {
                let targetY = centerY + k * 40;
                if (targetY >= pictureTop && targetY <= pictureTop + pictureHeight) {
                    const gradient = ctx.createLinearGradient(
                        screenX, slitPositions[i],
                        pictureX, targetY
                    );
                    gradient.addColorStop(0, 'rgba(255, 255, 255, 0.7)');
                    gradient.addColorStop(1, 'rgba(255, 255, 255, 0.1)');
                    ctx.beginPath();
                    ctx.moveTo(screenX, slitPositions[i]);
                    ctx.lineTo(pictureX, targetY);
                    ctx.strokeStyle = gradient;
                    ctx.stroke();
                }
            }
        }

        // Расчёт интенсивности
        const steps = 200;
        let intensities = [];
        let maxIntensity = 0;

        for (let i = 0; i <= steps; i++) {
            let t = i / steps;
            let yPos = pictureTop + t * pictureHeight;
            let angle = (yPos - centerY) * 0.002;
            let sinTheta = Math.sin(angle + angleRad);

            let phase = (2 * Math.PI * d_m * sinTheta) / lambda_m;

            let intensity;
            if (N === 2) {
                intensity = Math.pow(Math.cos(phase / 2), 2);
            } else {
                if (Math.abs(Math.sin(phase / 2)) < 1e-10) {
                    intensity = N * N;
                } else {
                    let numer = Math.sin(N * phase / 2);
                    let denom = Math.sin(phase / 2);
                    intensity = Math.pow(numer / denom, 2);
                }
                intensity = intensity / N;
            }

            intensities.push({ y: yPos, intensity });
            if (intensity > maxIntensity) maxIntensity = intensity;
        }

        // Отрисовка полос
        const rgb = wavelengthToRGB(lambda);
        ctx.shadowBlur = 20;
        ctx.shadowColor = `rgb(${rgb.r}, ${rgb.g}, ${rgb.b})`;

        for (let i = 0; i < intensities.length; i++) {
            let { y, intensity } = intensities[i];
            let norm = (maxIntensity > 0) ? intensity / maxIntensity : 0;
            let barWidth = Math.max(2, norm * 40);
            ctx.fillStyle = `rgb(${rgb.r}, ${rgb.g}, ${rgb.b})`;
            ctx.fillRect(pictureX - barWidth / 2, y - 1.5, barWidth, 3);
        }

        // Рамка вокруг картины
        ctx.shadowBlur = 20;
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 2;
        ctx.strokeRect(pictureX - 45, pictureTop - 5, 90, pictureHeight + 10);

        // Заголовок картины
        ctx.shadowBlur = 8;
        ctx.font = 'bold 16px "Segoe UI"';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'center';
        ctx.fillText('ИНТЕРФЕРЕНЦИОННАЯ КАРТИНА', pictureX, pictureTop - 15);
        ctx.textAlign = 'left';

        // Информационная строка с одним знаком после запятой для длины волны
        ctx.font = '11px monospace';
        ctx.fillStyle = '#ffffff';
        ctx.fillText(
            `λ = ${lambda.toFixed(1)} нм, d = ${d.toFixed(2)} мм, N = ${N}, угол падения α = ${angleDeg}°`,
            20, height - 20
        );

        ctx.shadowBlur = 0;
        ctx.shadowColor = 'transparent';
        ctx.globalAlpha = 1.0;
    }

    // Инициализация при загрузке страницы
    document.addEventListener('DOMContentLoaded', function() {
        // Устанавливаем начальные значения из serverData если они есть
        if (window.serverData) {
            const wavelengthInput = document.getElementById('wavelength');
            const distanceInput = document.getElementById('distance');
            const slitWidthInput = document.getElementById('slitWidth');
            const slitNumberInput = document.getElementById('slitNumber');
            const sourceAngleInput = document.getElementById('sourceAngle');

            if (wavelengthInput) wavelengthInput.value = window.serverData.wavelength;
            if (distanceInput) distanceInput.value = window.serverData.distance;
            if (slitWidthInput) slitWidthInput.value = window.serverData.slitWidth;
            if (slitNumberInput) slitNumberInput.value = window.serverData.slitNumber;
            if (sourceAngleInput) sourceAngleInput.value = window.serverData.sourceAngle;

            // Обновляем отображения
            updateValue(wavelengthInput, 'wavelengthValue', ' нм');
            updateValue(distanceInput, 'distanceValue', ' м');
            updateValue(slitWidthInput, 'slitWidthValue', ' мм');
            updateValue(slitNumberInput, 'slitNumberValue', '');
            updateValue(sourceAngleInput, 'angleValue', '°');
        }

        // Рисуем модель
        drawModel();
    });
})();